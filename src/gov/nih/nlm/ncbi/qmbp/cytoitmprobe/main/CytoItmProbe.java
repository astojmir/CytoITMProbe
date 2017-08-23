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
import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyMenus;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResultsFactory;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ResultsPanelManager;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.ImportSettings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.SwingConstants;
import javax.swing.event.MenuEvent;

public class CytoItmProbe extends CytoscapePlugin {

    private InputPanel queryTab = null;
    private String title = "CytoITMprobe";
    private String docUrl = "https://www.ncbi.nlm.nih.gov/CBBresearch/Yu/mn/itm_probe/doc/";

    public CytoItmProbe() {
        CyMenus menus = Cytoscape.getDesktop().getCyMenus();
        addAction(menus, title, new ITMProbeAction("Query Form"));
        addSeparator(menus, title);
        addAction(menus, title, new AboutAction(("About")));
        addAction(menus, title, new DocsAction(("Documentation")));

        ResultsPanelManager.initResultsPanelManager();
        
        // Import/Export menu
        addExportAction(menus);
        addImportAction(menus);

    }

    private static void addAction(CyMenus aMenus, String aSubMenu,
                                  CytoscapeAction aAction) {
            aAction.setPreferredMenu("Plugins." + aSubMenu);
            aMenus.addCytoscapeAction(aAction);
    }

    private void addExportAction(CyMenus aMenus) {
            CytoscapeAction aAction = new ItmExportAction();
            aAction.setPreferredMenu("File.Export");
            aMenus.addCytoscapeAction(aAction);
    }

    private void addImportAction(CyMenus aMenus) {
            CytoscapeAction aAction = new ItmImportAction();
            aAction.setPreferredMenu("File.Import");
            aMenus.addCytoscapeAction(aAction);
    }

    private static void addSeparator(CyMenus aMenus, String aSubMenu) {
        JMenu menuItem = null;
        for (final Component cmp : aMenus.getOperationsMenu().getMenuComponents()) {
                if (cmp instanceof JMenu && aSubMenu.equals(((JMenu) cmp).getText())) {
                        menuItem = (JMenu) cmp;
                        break;
                }
        }
        if (menuItem != null) {
                menuItem.addSeparator();
        }
    }

    public class AboutAction extends CytoscapeAction {
        public AboutAction(String itemName) {
            super(itemName);
        }
        public void actionPerformed(ActionEvent e) {
            AboutDialog aboutDialog = new AboutDialog(Cytoscape.getDesktop(),
                                                      true);
            aboutDialog.setLocationRelativeTo(Cytoscape.getDesktop());
            aboutDialog.setVisible(true);
        }
    }

    public class DocsAction extends CytoscapeAction {
        public DocsAction(String itemName) {
            super(itemName);
        }
        public void actionPerformed(ActionEvent e) {
            cytoscape.util.OpenBrowser.openURL(docUrl);
        }
    }

    public class ITMProbeAction extends CytoscapeAction {
	public ITMProbeAction(String itemName) {
            super(itemName);
        }

	public void actionPerformed(ActionEvent e) {
            CytoPanel ctrlPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
            if (queryTab == null) {
                queryTab = new InputPanel();
                ctrlPanel.add("ITM Probe", queryTab);
            }
            if (ctrlPanel.getState() == CytoPanelState.HIDE) {
                ctrlPanel.setState(CytoPanelState.DOCK);
            }
            ctrlPanel.setSelectedIndex(ctrlPanel.indexOfComponent(queryTab));
	}
    }
    
    public class ItmImportAction extends CytoscapeAction {
        public ItmImportAction() {
            super("Import ITM Probe Results from TAB File...");
            title = "Import ITM Probe Results from TAB File";
        }
        public void actionPerformed(ActionEvent e) {
            ImportSettings importer = new ImportSettings();
            importer.importFromTab();
        }
        @Override
        public void menuSelected(MenuEvent e) {
            CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();           
            if( cyNetwork != Cytoscape.getNullNetwork() ) {
                enableForNetwork();
            }
            else {
                setEnabled(false);
            }
        }
    }
 
    public class ItmExportAction extends CytoscapeAction {
        public ItmExportAction() {
            super("ITM Probe Results to TAB File...");
            title = "Export ITM Probe Results to TAB File";
        }
        public void actionPerformed(ActionEvent e) {
            ExportDialog exportDialog = new ExportDialog(Cytoscape.getDesktop(), true);
            exportDialog.setLocationRelativeTo(Cytoscape.getDesktop());
	    exportDialog.setVisible(true);
        }
        @Override
        public void menuSelected(MenuEvent e) {
            CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
            if (ItmProbeResultsFactory.hasStoredItms(cyNetwork, false)) {
                enableForNetwork();
            }
            else {
                setEnabled(false);
            }
        }
    }

}
