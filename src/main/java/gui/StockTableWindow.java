package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StockTableWindow extends JFrame { 

    public StockTableWindow(String stockStatusMessage) {
        setTitle("État du Stock");
        setSize(700, 300);
        setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
        setIconImage(icon.getImage());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        // En-têtes de colonnes
        String[] columnNames = {"Produit", "Stock", "Min", "Prix"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Nettoyage et parsing
        String cleaned = stockStatusMessage.replace("STOCK_STATUS|", "").trim();
        String[] lignes = cleaned.split(";");

        for (String ligne : lignes) {
            ligne = ligne.trim();
            if (ligne.isEmpty()) continue;

            try {
                String[] parts = ligne.split(" - ");
                if (parts.length == 3) {
                    String nom = parts[0].trim();
                    String stockMinPart = parts[1].trim();  // "Stock: 11 (Min: 5)"
                    String prixStr = parts[2].trim();

                    // Extraire stock et min
                    int stock = Integer.parseInt(stockMinPart.replaceAll("Stock: (\\d+).*", "$1"));
                    int min = Integer.parseInt(stockMinPart.replaceAll(".*Min: (\\d+).*", "$1"));

                    // Nettoyer le prix : enlever tout ce qui n'est pas chiffre ou virgule
                    String prixNumStr = prixStr.replaceAll("[^0-9,]", "")  // garde chiffres et virgule
                                              .replace(',', '.');           // convertit virgule en point

                    double prixVal = 0.0;
                    try {
                        prixVal = Double.parseDouble(prixNumStr);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    // Formatage du prix avec 2 décimales + Dh
                    String prix = String.format("%.2f Dh", prixVal);

                    model.addRow(new Object[]{nom, stock, min, prix});
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du parsing de la ligne : " + ligne);
                e.printStackTrace();
            }
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
}
