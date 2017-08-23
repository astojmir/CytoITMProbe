//
// ===========================================================================
//
//                            PUBLIC DOMAIN NOTICE
//               National Center for Biotechnology Information
//
//  This software/database is a "United States Government Work" under the
//  terms of the United States Copyright Act.  It was written as part of
//  the author's official duties as a United States Government employee and
//  thus cannot be copyrighted.  This software/database is freely available
//  to the public for use. The National Library of Medicine and the U.S.
//  Government have not placed any restriction on its use or reproduction.
//
//  Although all reasonable efforts have been taken to ensure the accuracy
//  and reliability of the software and data, the NLM and the U.S.
//  Government do not and cannot warrant the performance or results that
//  may be obtained by using this software or data. The NLM and the U.S.
//  Government disclaim all warranties, express or implied, including
//  warranties of performance, merchantability or fitness for any particular
//  purpose.
//
//  Please cite the author in any work or product based on this material.
//
// ===========================================================================
//
// Code authors:  Aleksandar Stojmirovic, Alexander Bliskovsky
//

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.main;

import cytoscape.CyNetwork;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.config.ConfigDialog;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.GraphInteraction;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.ListManager;
import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResultsFactory;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.AbsorbingSettings;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.ChannelSettings;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.EdgeTypes;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.EmittingSettings;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.ModelSettings;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class InputPanel extends javax.swing.JPanel {

    private DefaultListModel undirectedListModel = new DefaultListModel();
    private DefaultListModel directedListModel = new DefaultListModel();
    private DefaultListModel ignoredListModel = new DefaultListModel();

    private Set currentEdgeTypes;
    private ModelSettings [] modelSettings = {new ChannelSettings(),
                                              new EmittingSettings(),
                                              new AbsorbingSettings()};
    private PropertyChangeListener networkListener;

    /** Creates new form InputPanel */
    public InputPanel() {

        initComponents();

	undirectedList.setModel(undirectedListModel);
	directedList.setModel(directedListModel);
	ignoredList.setModel(ignoredListModel);
        setModelNames();
        reset();
        
        // Register a listener for network changes
        networkListener = new CurrentNetworkListener();
        CytoscapeDesktop cyDesktop = Cytoscape.getDesktop();
        cyDesktop.getSwingPropertyChangeSupport().addPropertyChangeListener(
                CytoscapeDesktop.NETWORK_VIEW_FOCUSED, networkListener);
        
    }

    private void setModelNames() {
        
        String [] modelNames = new String [modelSettings.length];
        for (int i=0; i < modelSettings.length; i++) {
            modelNames[i] = modelSettings[i].modelName;
        }
        modelDropdown.setModel(new javax.swing.DefaultComboBoxModel(modelNames));
      	modelDropdown.setSelectedIndex(0);
	modelDropdownActionPerformed(null);        
    }

    private EdgeTypes getInputEdgeTypes() {

        HashSet undirected = new HashSet<String>();
	HashSet directed = new HashSet<String>();
	HashSet ignored = new HashSet<String>();

	for(Enumeration e = undirectedListModel.elements(); e.hasMoreElements();) {
	    undirected.add(e.nextElement());
	}
	for(Enumeration e = directedListModel.elements(); e.hasMoreElements();) {
	    directed.add(e.nextElement());
	}
	for(Enumeration e = ignoredListModel.elements(); e.hasMoreElements();) {
	    ignored.add(e.nextElement());
	}
        
        EdgeTypes edgeTypes = new EdgeTypes(undirected, directed, ignored);
	return edgeTypes;
    }

    private void move(DefaultListModel fromModel, 
                      DefaultListModel toModel, 
                      int[] selectedIndices) {

	// Iterate in decreasing order such that the next objects are not affected
	for (int i = selectedIndices.length - 1; i > -1; i--) {
	    String identifier = (String)fromModel.get(selectedIndices[i]);
	    fromModel.remove(selectedIndices[i]);
	    toModel.add(0, identifier);
	}
    }

    private void refreshEdgeTypes(CyNetwork net) {

	Set<String> edgeTypes = GraphInteraction.getEdgeTypes(net);
	currentEdgeTypes = edgeTypes;

	directedListModel.clear();
	undirectedListModel.clear();
	ignoredListModel.clear();

	Iterator<String> typeIter = edgeTypes.iterator();
	while (typeIter.hasNext()) {
	    undirectedListModel.add(0, typeIter.next());
	}
    }

    private void refreshAttributeNames(CyNetwork net) {
	List<String> attributeNames = GraphInteraction.getEdgeAttributeNames(net);
	DefaultComboBoxModel model = new DefaultComboBoxModel();
	Iterator<String> iter = attributeNames.iterator();
	while (iter.hasNext()) {
    		model.addElement(iter.next());
	}
	weightDropdown.setModel(model);
    }

    private void reset() {
        
        CyNetwork net = Cytoscape.getCurrentNetwork();
	refreshEdgeTypes(net);
	refreshAttributeNames(net);

	sinksList.setModel(new DefaultListModel());
	sourcesList.setModel(new DefaultListModel());
	excludedList.setModel(new DefaultListModel());

	modelDropdown.setSelectedIndex(0);
	modelDropdownActionPerformed(null);

        if (ItmProbeResultsFactory.hasStoredItms(net, true)) {
            restoreButton.setEnabled(true);
        }
        else {
            restoreButton.setEnabled(false);
        }
    }
    
    private class CurrentNetworkListener implements  PropertyChangeListener {

        public CurrentNetworkListener() {
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equalsIgnoreCase(CytoscapeDesktop.NETWORK_VIEW_FOCUSED)) {
                String networkId = (String) e.getNewValue();
                CyNetwork net = Cytoscape.getNetwork(networkId);
                if (ItmProbeResultsFactory.hasStoredItms(net, true)) {
                    restoreButton.setEnabled(true);
                }
                else {
                    restoreButton.setEnabled(false);
                }
                refreshEdgeTypes(net);
            }
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane8 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        modelDropdown = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        undirectedList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        dToU = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        directedList = new javax.swing.JList();
        dToI = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        ignoredList = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        uToD = new javax.swing.JButton();
        iToD = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        weightDropdown = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourcesList = new javax.swing.JList();
        addSelectedSources = new javax.swing.JButton();
        removeSource = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        sinksList = new javax.swing.JList();
        addSelectedSinks = new javax.swing.JButton();
        removeSink = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        excludedList = new javax.swing.JList();
        addSelectedExcluded = new javax.swing.JButton();
        removeExcluded = new javax.swing.JButton();
        loadExclusions = new javax.swing.JButton();
        dissipationDropdown = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        dissipationBox = new javax.swing.JTextField();
        runButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        configButton = new javax.swing.JButton();
        restoreButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        setPreferredSize(new java.awt.Dimension(540, 675));
        setRequestFocusEnabled(false);

        jScrollPane8.setBorder(null);
        jScrollPane8.setPreferredSize(new java.awt.Dimension(540, 690));

        jPanel3.setPreferredSize(new java.awt.Dimension(540, 670));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 51, 102));
        jLabel5.setLabelFor(modelDropdown);
        jLabel5.setText("ITM Probe Model");
        jLabel5.setToolTipText("Choose ITM Probe model");
        jLabel5.setFocusTraversalPolicyProvider(true);

        modelDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        modelDropdown.setToolTipText("Choose ITM Probe model");
        modelDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelDropdownActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(27, 27, 27)
                .addComponent(modelDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(174, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(modelDropdown))
                .addContainerGap())
        );

        undirectedList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane3.setViewportView(undirectedList);

        jLabel6.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 102, 153));
        jLabel6.setText("EDGE ATTRIBUTES");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 51, 102));
        jLabel3.setLabelFor(undirectedList);
        jLabel3.setText("Undirected");
        jLabel3.setToolTipText("Select how to treat each edge type");

        dToU.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        dToU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/go-previous-5m.png"))); // NOI18N
        dToU.setToolTipText("Treat this edge type as undirected");
        dToU.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dToU.setPreferredSize(new java.awt.Dimension(32, 32));
        dToU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dToUActionPerformed(evt);
            }
        });

        directedList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane5.setViewportView(directedList);

        dToI.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        dToI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/go-next-5m.png"))); // NOI18N
        dToI.setToolTipText("Ignore this edge type");
        dToI.setMargin(new java.awt.Insets(2, 2, 2, 2));
        dToI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dToIActionPerformed(evt);
            }
        });

        ignoredList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane6.setViewportView(ignoredList);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 51, 102));
        jLabel7.setLabelFor(directedList);
        jLabel7.setText("Directed");
        jLabel7.setToolTipText("Select how to treat each edge type");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 51, 102));
        jLabel8.setLabelFor(directedList);
        jLabel8.setText("Ignored");
        jLabel8.setToolTipText("Select how to treat each edge type");

        uToD.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        uToD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/go-next-5m.png"))); // NOI18N
        uToD.setToolTipText("Treat this edge type as directed");
        uToD.setMargin(new java.awt.Insets(2, 2, 2, 2));
        uToD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uToDActionPerformed(evt);
            }
        });

        iToD.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        iToD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/go-previous-5m.png"))); // NOI18N
        iToD.setToolTipText("Treat this edge type as directed");
        iToD.setMargin(new java.awt.Insets(0, 0, 0, 0));
        iToD.setPreferredSize(new java.awt.Dimension(32, 32));
        iToD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iToDActionPerformed(evt);
            }
        });

        refreshButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/view-refresh-4.png"))); // NOI18N
        refreshButton.setToolTipText("Refresh edge types");
        refreshButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        refreshButton.setPreferredSize(new java.awt.Dimension(32, 32));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 51, 102));
        jLabel11.setText("Treat edge types as undirected, directed or ignored ...");
        jLabel11.setToolTipText("Select how to treat each edge type");

        weightDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        weightDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NONE (Default)" }));
        weightDropdown.setToolTipText("Choose edge attribute to be used as edge weights");

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 51, 102));
        jLabel12.setLabelFor(weightDropdown);
        jLabel12.setText("Weight Attribute");
        jLabel12.setToolTipText("Choose edge attribute to be used as edge weights");

        jLabel9.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 102, 153));
        jLabel9.setText("MODEL PARAMETERS");

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 51, 102));
        jLabel1.setLabelFor(sourcesList);
        jLabel1.setText("Sources");

        jScrollPane1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        sourcesList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        sourcesList.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(sourcesList);

        addSelectedSources.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        addSelectedSources.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/list-add-3.png"))); // NOI18N
        addSelectedSources.setToolTipText("Add selected nodes to sources");
        addSelectedSources.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addSelectedSources.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedSourcesActionPerformed(evt);
            }
        });

        removeSource.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        removeSource.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/list-remove-3.png"))); // NOI18N
        removeSource.setToolTipText("Remove nodes from the sources list");
        removeSource.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSourceActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 51, 102));
        jLabel10.setLabelFor(sinksList);
        jLabel10.setText("Sinks");

        sinksList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        sinksList.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(sinksList);

        addSelectedSinks.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        addSelectedSinks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/list-add-3.png"))); // NOI18N
        addSelectedSinks.setToolTipText("Add selected nodes to sinks");
        addSelectedSinks.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addSelectedSinks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedSinksActionPerformed(evt);
            }
        });

        removeSink.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        removeSink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/list-remove-3.png"))); // NOI18N
        removeSink.setToolTipText("Remove nodes from the sinks list");
        removeSink.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeSink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSinkActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 51, 102));
        jLabel4.setLabelFor(excludedList);
        jLabel4.setText("Excluded Nodes");
        jLabel4.setToolTipText("Add selected nodes to excluded nodes");

        excludedList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        excludedList.setModel(new DefaultListModel());
        jScrollPane4.setViewportView(excludedList);

        addSelectedExcluded.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        addSelectedExcluded.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/list-add-3.png"))); // NOI18N
        addSelectedExcluded.setToolTipText("Add Selected Nodes");
        addSelectedExcluded.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addSelectedExcluded.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedExcludedActionPerformed(evt);
            }
        });

        removeExcluded.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        removeExcluded.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/list-remove-3.png"))); // NOI18N
        removeExcluded.setToolTipText("Remove nodes from the excluded nodes list");
        removeExcluded.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeExcluded.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeExcludedActionPerformed(evt);
            }
        });

        loadExclusions.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        loadExclusions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/document-open-5.png"))); // NOI18N
        loadExclusions.setToolTipText("Load excluded nodes from file");
        loadExclusions.setMargin(new java.awt.Insets(0, 0, 0, 0));
        loadExclusions.setPreferredSize(new java.awt.Dimension(32, 32));
        loadExclusions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadExclusionsActionPerformed(evt);
            }
        });

        dissipationDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dissipationDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Termination (dissipating) probability", "Expected drift from shortest path (absolute)", "Expected drift from shortest path (relative)" }));
        dissipationDropdown.setToolTipText("Choose a dissipation criterion");
        dissipationDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dissipationDropdownActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 51, 102));
        jLabel2.setText("Dissipation Criterion");
        jLabel2.setToolTipText("Choose a dissipation criterion");

        dissipationBox.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dissipationBox.setToolTipText("Enter the value of dissipation criterion here");

        runButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        runButton.setText("RUN");
        runButton.setToolTipText("Run ITM Probe");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        resetButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        resetButton.setText("RESET");
        resetButton.setToolTipText("Reset the form");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        configButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        configButton.setText("CONFIG");
        configButton.setToolTipText("Configure local and web paths");
        configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });

        restoreButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        restoreButton.setText("LOAD");
        restoreButton.setToolTipText("Restore stored ITM from current network attributes.");
        restoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(32, 32, 32)
                .addComponent(weightDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jLabel6)
            .addComponent(jLabel11)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dToU, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(uToD, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(iToD, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dToI, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel7))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addSelectedSources, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(removeSource, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addSelectedSinks, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(removeSink, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(dissipationBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(12, 12, 12)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(loadExclusions, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(removeExcluded, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(addSelectedExcluded, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))))))))
            .addComponent(jLabel9)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(configButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(restoreButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jLabel2)
            .addComponent(dissipationDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(weightDropdown))
                .addGap(12, 12, 12)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(iToD, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dToI, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(dToU, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uToD, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(addSelectedSources, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeSource, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(addSelectedSinks, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeSink, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(addSelectedExcluded, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeExcluded, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loadExclusions, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dissipationBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dissipationDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runButton)
                    .addComponent(resetButton)
                    .addComponent(configButton)
                    .addComponent(restoreButton))
                .addContainerGap())
        );

        jLabel13.setBackground(new java.awt.Color(51, 51, 51));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/nih/nlm/ncbi/qmbp/cytoitmprobe/resources/banner-itmprobe.png"))); // NOI18N
        jLabel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jLabel13.setOpaque(true);
        jLabel13.setPreferredSize(new java.awt.Dimension(540, 77));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane8.setViewportView(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void loadExclusionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadExclusionsActionPerformed

        JFileChooser exclusionsChooser= new JFileChooser();
	int returnVal = exclusionsChooser.showOpenDialog(this);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    ListManager.loadExclusions(excludedList,
				       exclusionsChooser.getSelectedFile());
        }
        
    }//GEN-LAST:event_loadExclusionsActionPerformed

    private void modelDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelDropdownActionPerformed

	Color disabledColor = new Color(200,200,200);
	Color enabledColor = new Color(255,255,255);

	int i = modelDropdown.getSelectedIndex();
        ModelSettings currentModel = modelSettings[i];
        
        if (currentModel.hasSources) { 
	    sourcesList.setEnabled(true);
	    addSelectedSources.setEnabled(true);
	    removeSource.setEnabled(true);
	    sourcesList.setBackground(enabledColor);           
        }
        else {
	    sourcesList.setEnabled(false);
	    addSelectedSources.setEnabled(false);
	    removeSource.setEnabled(false);
	    sourcesList.setBackground(disabledColor);            
        }
            
        if (currentModel.hasSinks) { // has sinks
	    sinksList.setEnabled(true);
	    addSelectedSinks.setEnabled(true);
	    removeSink.setEnabled(true);
	    sinksList.setBackground(enabledColor);            
        }
        else {
	    sinksList.setEnabled(false);
	    addSelectedSinks.setEnabled(false);
	    removeSink.setEnabled(false);
	    sinksList.setBackground(disabledColor);           
        }

        String [] dampingParams = currentModel.dampingParamNames;
	dissipationDropdown.setModel(new DefaultComboBoxModel(dampingParams));
	changeDropdownValue();
    }//GEN-LAST:event_modelDropdownActionPerformed

    private void changeDropdownValue() {
        int i = modelDropdown.getSelectedIndex();
        ModelSettings currentModel = modelSettings[i];
        int j = dissipationDropdown.getSelectedIndex();
        dissipationBox.setText(currentModel.dampingParamDefaults[j]);
    }

    private void dissipationDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dissipationDropdownActionPerformed
	changeDropdownValue();
    }//GEN-LAST:event_dissipationDropdownActionPerformed

    private void dToUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dToUActionPerformed
	int [] selectedIndices = directedList.getSelectedIndices();
	move(directedListModel, undirectedListModel, selectedIndices);
    }//GEN-LAST:event_dToUActionPerformed

    private void dToIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dToIActionPerformed
        int [] selectedIndices = directedList.getSelectedIndices();
	move(directedListModel, ignoredListModel, selectedIndices);
    }//GEN-LAST:event_dToIActionPerformed

    private void uToDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uToDActionPerformed
 	int[] selectedIndices = undirectedList.getSelectedIndices();
	move(undirectedListModel, directedListModel, selectedIndices);
    }//GEN-LAST:event_uToDActionPerformed

    private void iToDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iToDActionPerformed
	int [] selectedIndices = ignoredList.getSelectedIndices();
	move(ignoredListModel, directedListModel, selectedIndices);
    }//GEN-LAST:event_iToDActionPerformed

    private void addSelectedSourcesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedSourcesActionPerformed
	Set<String> selectedNodes = GraphInteraction.getSelectedNodes();
	ListManager.appendSet(sourcesList, selectedNodes);
    }//GEN-LAST:event_addSelectedSourcesActionPerformed

    private void removeSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSourceActionPerformed
	ListManager.removeNodes(sourcesList);
    }//GEN-LAST:event_removeSourceActionPerformed

    private void addSelectedSinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedSinksActionPerformed
	Set<String> selectedNodes = GraphInteraction.getSelectedNodes();
	ListManager.appendSet(sinksList, selectedNodes);
    }//GEN-LAST:event_addSelectedSinksActionPerformed

    private void removeSinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSinkActionPerformed
	ListManager.removeNodes(sinksList);
    }//GEN-LAST:event_removeSinkActionPerformed

    private void addSelectedExcludedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedExcludedActionPerformed
	Set<String> selectedNodes = GraphInteraction.getSelectedNodes();
	ListManager.appendSet(excludedList, selectedNodes);
    }//GEN-LAST:event_addSelectedExcludedActionPerformed

    private void removeExcludedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeExcludedActionPerformed
	ListManager.removeNodes(excludedList);
    }//GEN-LAST:event_removeExcludedActionPerformed


    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed


        // Retrieve parameters from the form
        int modelIndex = modelDropdown.getSelectedIndex();
        ModelSettings currentModel = modelSettings[modelIndex];
        
        String weightAttr = (String) weightDropdown.getSelectedItem();
        List<String> sources = ListManager.getNodesFromList(sourcesList.getModel()); 
        List<String> sinks = ListManager.getNodesFromList(sinksList.getModel()); 
        Map<String, Double> antisinks = ListManager.getAntiSinks(excludedList.getModel()); 
        int dampingIndex = dissipationDropdown.getSelectedIndex(); 
        String dampingValue = dissipationBox.getText();
        
        // Validate parameters
	String validationMessage = currentModel.validate(weightAttr, 
                                                         currentEdgeTypes, 
                                                         sources, 
                                                         sinks, 
                                                         antisinks, 
                                                         dampingIndex, 
                                                         dampingValue);
	if (validationMessage != null) {
	    JOptionPane.showMessageDialog(this,
					  validationMessage,
					  "Invalid Input",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}

        String errMsg = currentModel.queryItmProbe(weightAttr, 
                                                   getInputEdgeTypes(), 
                                                   sources, 
                                                   sinks, 
                                                   antisinks, 
                                                   dampingIndex, 
                                                   dampingValue);


	if (errMsg != null) {
	    JOptionPane.showMessageDialog(this,
					  errMsg,
					  "ITM Probe Error",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}
    }//GEN-LAST:event_runButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        reset();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
	ConfigDialog configDialog = new ConfigDialog(Cytoscape.getDesktop(), true);
        configDialog.setLocationRelativeTo(this);
	configDialog.setVisible(true);
    }//GEN-LAST:event_configButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        CyNetwork net = Cytoscape.getCurrentNetwork();
        refreshEdgeTypes(net);
	refreshAttributeNames(net);
    }//GEN-LAST:event_refreshButtonActionPerformed

private void restoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreButtonActionPerformed
        RestoreDialog restoreDialog = new RestoreDialog(Cytoscape.getDesktop(), true);
        restoreDialog.setLocationRelativeTo(Cytoscape.getDesktop());
	restoreDialog.setVisible(true);
}//GEN-LAST:event_restoreButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSelectedExcluded;
    private javax.swing.JButton addSelectedSinks;
    private javax.swing.JButton addSelectedSources;
    private javax.swing.JButton configButton;
    private javax.swing.JButton dToI;
    private javax.swing.JButton dToU;
    private javax.swing.JList directedList;
    private javax.swing.JTextField dissipationBox;
    private javax.swing.JComboBox dissipationDropdown;
    private javax.swing.JList excludedList;
    private javax.swing.JButton iToD;
    private javax.swing.JList ignoredList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JButton loadExclusions;
    private javax.swing.JComboBox modelDropdown;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton removeExcluded;
    private javax.swing.JButton removeSink;
    private javax.swing.JButton removeSource;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton restoreButton;
    private javax.swing.JButton runButton;
    private javax.swing.JList sinksList;
    private javax.swing.JList sourcesList;
    private javax.swing.JButton uToD;
    private javax.swing.JList undirectedList;
    private javax.swing.JComboBox weightDropdown;
    // End of variables declaration//GEN-END:variables

}
