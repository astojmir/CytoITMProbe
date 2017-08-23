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

import java.util.Vector;
import javax.swing.SwingConstants;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResults;
import cytoscape.Cytoscape;
import cytoscape.view.cytopanels.CytoPanelImp;
import javax.swing.JOptionPane;

/**
 *
 * @author aleksand
 */
public class OutputPanel extends javax.swing.JPanel {

    public ItmProbeResults results;
    private boolean wasMixedColoring;

    /** Creates new form OutputPanel */
    public OutputPanel(ItmProbeResults results) {
        this.results = results;
        initComponents();
        Object [] tableData = results.createSubNetworkView();
        resetNodeTable((Vector) tableData[2], (Vector) tableData[1]);
        resetDisplayForm();
        resetColormaps();
    }

   private void resetNodeTable(Vector body, Vector header) {
        nodesTable.setModel(new javax.swing.table.DefaultTableModel(body, header)
        {
            @Override
            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return java.lang.Integer.class;
                }
                else if (columnIndex == 1) {
                    return java.lang.String.class;
                }
                else {
                    return java.lang.Double.class;
                }
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        javax.swing.table.TableColumn column = null;
        for (int i = 0; i < header.size(); i++) {
            column = nodesTable.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(30);
            }
            else {
                column.setPreferredWidth(60);
            }
        }
   }

    private void resetColormaps() {
        colormapDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getMonoColormaps()));
        colormapDropdown.setSelectedIndex(results.getDefaultColormap());
    }
   
    private void resetDisplayForm() {
        rankingDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getRankingAttributes()));
        rankingDropdown.setSelectedIndex(results.getDefaultRankingAttribute());
        selectionDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getSelectionCriteria()));
        selectionDropdown.setSelectedIndex(results.getDefaultSelectionCriterion());
        cutoffBox.setText(String.format("%f", results.getDefaultCutoffValue()));
        int cutoffIndex = results.getDefaultSelectionCriterion();
        if (results.isCutoffSelection(cutoffIndex)) {
            jLabel9.setEnabled(true);
            cutoffBox.setEnabled(true);
        }
        else {
            jLabel9.setEnabled(false);
            cutoffBox.setEnabled(false);
        }
        maxnodesBox.setText(String.format("%d", results.getDefaultMaxNodes()));
        coloringList.setModel(new javax.swing.AbstractListModel() {
          String[] strings = results.getColoringAttributes();
          public int getSize() { return strings.length; }
          public Object getElementAt(int i) { return strings[i]; }
        });
        coloringList.setSelectedIndex(results.getDefaultColoringAttribute());
        
        int colorIndex = results.getDefaultColoringAttribute();
        wasMixedColoring = false;
        if (wasMixedColoring) {
            scalingDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getMixedScalings()));
            scalingDropdown.setSelectedIndex(results.getDefaultMixedScaling());
            jLabel13.setEnabled(false);
            colormapDropdown.setEnabled(false);
        }
        else {
            scalingDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getMonoScalings()));
            scalingDropdown.setSelectedIndex(results.getDefaultMonoScaling());
            jLabel13.setEnabled(true);
            colormapDropdown.setEnabled(true);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        displayPanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        displayContainer = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        coloringList = new javax.swing.JList();
        jLabel13 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        scalingDropdown = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        selectionDropdown = new javax.swing.JComboBox();
        maxnodesBox = new javax.swing.JTextField();
        rankingDropdown = new javax.swing.JComboBox();
        colormapDropdown = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        cutoffBox = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        nodeValsPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        nodesTable = new javax.swing.JTable();
        summaryPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        summaryTable = new javax.swing.JTable();
        paramsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inputParamsTable = new javax.swing.JTable();
        excludedPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        excludedText = new javax.swing.JTextArea();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jTabbedPane1.setForeground(new java.awt.Color(0, 51, 102));
        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(380, 637));

        displayPanel.setMaximumSize(new java.awt.Dimension(600, 400));
        displayPanel.setPreferredSize(new java.awt.Dimension(382, 400));

        jScrollPane6.setBorder(null);
        jScrollPane6.setPreferredSize(new java.awt.Dimension(443, 450));

        displayContainer.setPreferredSize(new java.awt.Dimension(443, 444));

        coloringList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        coloringList.setToolTipText("Choose the attribute(s) for coloring the nodes");
        coloringList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                coloringListValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(coloringList);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 51, 102));
        jLabel13.setText("Color Map");
        jLabel13.setToolTipText("Choose color map (Brewer colors with 8 steps).");

        jLabel7.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(51, 102, 153));
        jLabel7.setText("NODE SELECTION OPTIONS");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 51, 102));
        jLabel10.setText("Maximum Nodes Shown");
        jLabel10.setToolTipText("Choose the maximum number of nodes in the subgraph. This overrides all other selection criteria.");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 51, 102));
        jLabel8.setText("Selection Criterion");
        jLabel8.setToolTipText("Choose the criterion for ranking cutoff");

        resetButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        resetButton.setText("RESET");
        resetButton.setToolTipText("Reset form");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 51, 102));
        jLabel9.setText("Cutoff Value");
        jLabel9.setToolTipText("Enter cutoff value (only used if 'Cutoff Value' is the selection criterion");

        runButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        runButton.setText("RENDER");
        runButton.setToolTipText("Render ITM subgraph and show top scoring nodes by ranking attribute.");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        scalingDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        scalingDropdown.setToolTipText("Choose scaling function before coloring.");

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 51, 102));
        jLabel12.setText("Scaling");
        jLabel12.setToolTipText("Choose scaling function before coloring");

        selectionDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        selectionDropdown.setToolTipText("Choose the criterion for ranking cutoff");
        selectionDropdown.setLightWeightPopupEnabled(false);
        selectionDropdown.setMaximumSize(new java.awt.Dimension(300, 26));
        selectionDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionDropdownActionPerformed(evt);
            }
        });

        maxnodesBox.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        maxnodesBox.setToolTipText("Choose the maximum number of nodes in the subgraph. This overrides all other selection criteria.");

        rankingDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        rankingDropdown.setToolTipText("Select a node attribute to rank by");

        colormapDropdown.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        colormapDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Blues8", " " }));
        colormapDropdown.setToolTipText("Choose color map (Brewer colors with 8 steps).");

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 51, 102));
        jLabel11.setLabelFor(coloringList);
        jLabel11.setText("Coloring Attributes");
        jLabel11.setToolTipText("Choose the attribute(s) for coloring the nodes");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 51, 102));
        jLabel5.setText("Ranking Attribute");
        jLabel5.setToolTipText("Select a node attribute to rank by");

        closeButton.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        closeButton.setText("CLOSE");
        closeButton.setToolTipText("Close panel.");
        closeButton.setAutoscrolls(true);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        cutoffBox.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        cutoffBox.setToolTipText("Enter cutoff value (only used if 'Cutoff Value' is the selection criterion");

        jLabel6.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 102, 153));
        jLabel6.setText("NODE COLORING OPTIONS");

        javax.swing.GroupLayout displayContainerLayout = new javax.swing.GroupLayout(displayContainer);
        displayContainer.setLayout(displayContainerLayout);
        displayContainerLayout.setHorizontalGroup(
            displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(displayContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(displayContainerLayout.createSequentialGroup()
                        .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel6)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(scalingDropdown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colormapDropdown, 0, 223, Short.MAX_VALUE)
                            .addComponent(maxnodesBox)
                            .addComponent(cutoffBox)
                            .addComponent(selectionDropdown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rankingDropdown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(displayContainerLayout.createSequentialGroup()
                        .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(displayContainerLayout.createSequentialGroup()
                                .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        displayContainerLayout.setVerticalGroup(
            displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(displayContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(rankingDropdown))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectionDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cutoffBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxnodesBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel11)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(scalingDropdown))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(colormapDropdown))
                .addGap(18, 18, 18)
                .addGroup(displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runButton)
                    .addComponent(closeButton)
                    .addComponent(resetButton))
                .addContainerGap())
        );

        displayContainerLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {colormapDropdown, cutoffBox, maxnodesBox, rankingDropdown, scalingDropdown, selectionDropdown});

        jScrollPane6.setViewportView(displayContainer);

        javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
        displayPanel.setLayout(displayPanelLayout);
        displayPanelLayout.setHorizontalGroup(
            displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
        );
        displayPanelLayout.setVerticalGroup(
            displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Display Options", displayPanel);

        nodeValsPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        nodeValsPanel.setLayout(new javax.swing.BoxLayout(nodeValsPanel, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        nodesTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        nodesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        nodesTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(nodesTable);
        class DoubleRenderer extends javax.swing.table.DefaultTableCellRenderer {
            public void setValue(Object value) {
                setText((value == null) ? "" : String.format("%.2g", value));
            }
        }
        nodesTable.setDefaultRenderer(java.lang.Double.class, new DoubleRenderer());

        nodeValsPanel.add(jScrollPane4);

        jTabbedPane1.addTab("Top Scoring Nodes", nodeValsPanel);

        summaryPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        summaryPanel.setLayout(new javax.swing.BoxLayout(summaryPanel, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        summaryTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        summaryTable.setModel(new javax.swing.table.DefaultTableModel(
            results.getSummary(),
            results.getSummaryHeader())
        {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        summaryTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        summaryTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(summaryTable);

        summaryPanel.add(jScrollPane2);

        jTabbedPane1.addTab("Summary", summaryPanel);

        paramsPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        paramsPanel.setLayout(new javax.swing.BoxLayout(paramsPanel, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        inputParamsTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        inputParamsTable.setModel(new javax.swing.table.DefaultTableModel(
            results.getInputParameters(),
            results.getInputParametersHeader())
        {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        inputParamsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        inputParamsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(inputParamsTable);

        paramsPanel.add(jScrollPane1);

        jTabbedPane1.addTab("Input Parameters", paramsPanel);

        excludedPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        excludedPanel.setLayout(new javax.swing.BoxLayout(excludedPanel, javax.swing.BoxLayout.LINE_AXIS));

        excludedText.setColumns(20);
        excludedText.setEditable(false);
        excludedText.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        excludedText.setLineWrap(true);
        excludedText.setRows(5);
        excludedText.setText(results.getExcludedNodes());
        excludedText.setWrapStyleWord(true);
        jScrollPane3.setViewportView(excludedText);

        excludedPanel.add(jScrollPane3);

        jTabbedPane1.addTab("Excluded Nodes", excludedPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectionDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionDropdownActionPerformed
        int index = selectionDropdown.getSelectedIndex();
        if (results.isCutoffSelection(index)) {
            jLabel9.setEnabled(true);
            cutoffBox.setEnabled(true);
        }
        else {
            jLabel9.setEnabled(false);
            cutoffBox.setEnabled(false);
        }
}//GEN-LAST:event_selectionDropdownActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetDisplayForm();
    }//GEN-LAST:event_resetButtonActionPerformed

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
    Cytoscape.destroyNetwork(results.getSubNetwork());
}//GEN-LAST:event_closeButtonActionPerformed

private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed

    String validationMessage = null;
    // Get node selection parameters and validate
    int rankingIndex = rankingDropdown.getSelectedIndex();
    int criterionIndex = selectionDropdown.getSelectedIndex();
    Double cutoffValue = null;
    Integer maxNodes = 0;

    int [] coloringIndices = coloringList.getSelectedIndices();
    int scalingIndex = scalingDropdown.getSelectedIndex();
    int colormapIndex = colormapDropdown.getSelectedIndex();
    
    // Validate cutoffValue
    if (cutoffBox.isEnabled()) {
        try {
            cutoffValue = new Double(cutoffBox.getText());
        }
        catch (Exception e) {
            validationMessage = "Invalid cutoff value.";
        }
    }

    // Validate maxNodes
    try {
        maxNodes = new Integer(maxnodesBox.getText());
    }
    catch (Exception e) {
        validationMessage = "Invalid maximum nodes value.";
    }
    if (maxNodes < 0) {
        validationMessage = "Maximum nodes value must be positive.";
    }

    // Validate coloringIndices
    if (coloringIndices.length > 3) {
        validationMessage = "Can only select up to three coloring" + 
                            " attributes.\n\n" +
                            "Color mixture display allows mixing of at most " + 
                            "three color channels\n" +
                            "corresponding to cyan, magenta and yellow\n";
    }
    
    if (validationMessage != null) {
        JOptionPane.showMessageDialog(this,
                                      validationMessage,
                                      "Invalid Input",
                                      JOptionPane.ERROR_MESSAGE);
        return;
    }

    Object [] tableData = results.updateSubNetworkView(rankingIndex,
                                                       criterionIndex,
                                                       cutoffValue,
                                                       maxNodes.intValue(),
                                                       coloringIndices,
                                                       scalingIndex,
                                                       colormapIndex);
    resetNodeTable((Vector) tableData[2], (Vector) tableData[1]);

}//GEN-LAST:event_runButtonActionPerformed

private void coloringListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_coloringListValueChanged
        
    
        if (evt.getValueIsAdjusting()) {
            return;
        }
        int [] colorIndices = coloringList.getSelectedIndices();
        boolean isMixedColoring = (colorIndices.length > 1);
        if (isMixedColoring != wasMixedColoring) {
            if (isMixedColoring) {
                scalingDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getMixedScalings()));
                scalingDropdown.setSelectedIndex(results.getDefaultMixedScaling());
                jLabel13.setEnabled(false);
                colormapDropdown.setEnabled(false);
            }
            else {
                scalingDropdown.setModel(new javax.swing.DefaultComboBoxModel(results.getMonoScalings()));
                scalingDropdown.setSelectedIndex(results.getDefaultMonoScaling());
                jLabel13.setEnabled(true);
                colormapDropdown.setEnabled(true);
           }
        wasMixedColoring = isMixedColoring;
        }
}//GEN-LAST:event_coloringListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JList coloringList;
    private javax.swing.JComboBox colormapDropdown;
    private javax.swing.JTextField cutoffBox;
    private javax.swing.JPanel displayContainer;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JPanel excludedPanel;
    private javax.swing.JTextArea excludedText;
    private javax.swing.JTable inputParamsTable;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField maxnodesBox;
    private javax.swing.JPanel nodeValsPanel;
    private javax.swing.JTable nodesTable;
    private javax.swing.JPanel paramsPanel;
    private javax.swing.JComboBox rankingDropdown;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton runButton;
    private javax.swing.JComboBox scalingDropdown;
    private javax.swing.JComboBox selectionDropdown;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JTable summaryTable;
    // End of variables declaration//GEN-END:variables

}
