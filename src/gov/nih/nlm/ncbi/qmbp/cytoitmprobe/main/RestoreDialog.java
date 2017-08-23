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
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResults;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResultsFactory;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ResultsPanelManager;
import java.util.List;

/**
 *
 * @author stojmira
 */
public class RestoreDialog extends javax.swing.JDialog {

    List<String []> itmData;
    
    /** Creates new form RestoreDialog */
    public RestoreDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        populateItmList();
    }

    private void populateItmList() {
        CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        itmData = ItmProbeResultsFactory.findStoredItms(currentNetwork, true);
        if (itmData.isEmpty()) {
            return;
        }
        itmList.setModel(new javax.swing.AbstractListModel() {
          public int getSize() { return itmData.size(); }
          public Object getElementAt(int i) { return itmData.get(i)[1]; }
        });
        itmList.setSelectedIndex(0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        itmList = new javax.swing.JList();
        OkButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Restore ITM From Current Network");
        setMinimumSize(new java.awt.Dimension(589, 232));
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14));
        jLabel11.setForeground(new java.awt.Color(0, 51, 102));
        jLabel11.setText("Choose ITMs to restore and insert into Results Panel");
        jLabel11.setToolTipText("");

        itmList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jScrollPane1.setViewportView(itmList);

        OkButton.setFont(new java.awt.Font("Arial", 0, 15));
        OkButton.setText("OK");
        OkButton.setToolTipText("Load selected ITMs");
        OkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Arial", 0, 15));
        cancelButton.setText("CANCEL");
        cancelButton.setToolTipText("Close this window");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 557, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, layout.createSequentialGroup()
                        .add(OkButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel11))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(jLabel11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(OkButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void OkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkButtonActionPerformed

        int [] itmIndices = itmList.getSelectedIndices();
        
        ItmRestore task = new ItmRestore(itmIndices);
        // Configure JTask Dialog Pop-Up Box
        JTaskConfig jTaskConfig = new JTaskConfig();
        jTaskConfig.setOwner(Cytoscape.getDesktop());
        jTaskConfig.displayCloseButton(true);
        jTaskConfig.displayCancelButton(true);

        jTaskConfig.displayStatus(true);
        jTaskConfig.setAutoDispose(true);
        jTaskConfig.displayTimeElapsed(true);
        jTaskConfig.displayTimeRemaining(false);
        
        // Execute Task in New Thread; pops open JTask Dialog Box.
        TaskManager.executeTask(task, jTaskConfig);
	setVisible(false);
	dispose();
}//GEN-LAST:event_OkButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
	setVisible(false);
	dispose();

}//GEN-LAST:event_cancelButtonActionPerformed
    private class ItmRestore implements Task {
        
        private cytoscape.task.TaskMonitor taskMonitor;
        private int [] itmIndices;
        boolean isHalted = false;
        
        public ItmRestore(int[] itmIndices) {
            this.itmIndices = itmIndices;
        }

        public void setTaskMonitor(TaskMonitor monitor)
                        throws IllegalThreadStateException {
            taskMonitor = monitor;
        }

        public void halt() {
            isHalted = true;
        }

        public String getTitle() {
            return "Loading ITMs from network attributes";
        }

        public void run() {
            
            try {
                taskMonitor.setPercentCompleted(0);
                taskMonitor.setStatus("Restoring ITMs");

                CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
                for (int i=0; i < itmIndices.length; i++) {
                    if (isHalted) {
                        return;
                    }
                    int p = 100 * i / itmIndices.length;
                    taskMonitor.setPercentCompleted(p);
                    String queryPrefix = itmData.get(itmIndices[i])[0];
                    ItmProbeResults results = ItmProbeResultsFactory.fromCyNetwork(
                        currentNetwork, queryPrefix);
                    ResultsPanelManager.addResults(results);
                }        

                taskMonitor.setPercentCompleted(100);
            }
            catch (Exception ex) {
                taskMonitor.setException(ex, null);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RestoreDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RestoreDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RestoreDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RestoreDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                RestoreDialog dialog = new RestoreDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OkButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList itmList;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}