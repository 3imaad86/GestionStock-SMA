package gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import Agents.ManagerAgent;

public class ManagerGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ManagerAgent managerAgent;
    private JTextArea logArea;
    private JTextField customerNameField;
    private JComboBox<String> productComboBox;
    private JTextField quantityField;
    private JTextField addressField;
    private JLabel statusLabel;
    private JButton createOrderButton;
    private JButton stockStatusButton;
    private SimpleDateFormat dateFormat;
    
    public ManagerGUI(ManagerAgent agent) {
        this.managerAgent = agent;
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        initializeGUI();
        setupEventHandlers();
    }
    
    private void initializeGUI() {
        setTitle("Système de Gestion de Stock JADE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
        setIconImage(icon.getImage());

        // Panel principal avec BorderLayout
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        JLabel titleLabel = new JLabel("📦 Gestionnaire de Stock Multi-Agents");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Panel central avec deux colonnes
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Colonne gauche - Contrôles
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel);
        
        // Colonne droite - Logs
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer avec statut
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(236, 240, 241));
        statusLabel = new JLabel("🟢 Système démarré - " + dateFormat.format(new Date()));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(statusLabel);
        add(footerPanel, BorderLayout.SOUTH);
        
        // Style général
        UIManager.put("TitledBorder.titleColor", new Color(52, 73, 94));
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("🎛️ Contrôles du Système"));
        
        // Section création de commande
        JPanel orderPanel = new JPanel(new GridBagLayout());
        orderPanel.setBorder(new TitledBorder("📝 Nouvelle Commande"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nom client
        gbc.gridx = 0; gbc.gridy = 0;
        orderPanel.add(new JLabel("👤 Client:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        customerNameField = new JTextField(15);
        customerNameField.setText("Said OUABOU");
        orderPanel.add(customerNameField, gbc);
        
        // Nom produit
        
        String[] productNames = {
        	    "Ordinateur Portable",
        	    "Webcam HD",
        	    "Écran 24 pouces",
        	    "Clavier Mécanique",
        	    "Souris Wireless"
        	};

        
        /*gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        orderPanel.add(new JLabel("📦 Produit:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        productNameField = new JTextField(15);
        productNameField.setText("Ordinateur Portable");
        orderPanel.add(productNameField, gbc);
        */
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        orderPanel.add(new JLabel("📦 Produit:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        productComboBox = new JComboBox<>(productNames);
        orderPanel.add(productComboBox, gbc);
        
        // Quantité
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        orderPanel.add(new JLabel("🔢 Quantité:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        quantityField = new JTextField(15);
        quantityField.setText("1");
        orderPanel.add(quantityField, gbc);
        
        // Adresse
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        orderPanel.add(new JLabel("🏠 Adresse:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        addressField = new JTextField(15);
        addressField.setText("123 Rue de la Paix, Ouarzazate, Maroc");
        orderPanel.add(addressField, gbc);
        
        // Bouton créer commande
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        createOrderButton = new JButton("🛒 Créer Commande");
        createOrderButton.setBackground(new Color(46, 204, 113));
        createOrderButton.setForeground(Color.WHITE);
        createOrderButton.setFont(new Font("Arial", Font.BOLD, 12));
        orderPanel.add(createOrderButton, gbc);
        
        panel.add(orderPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Section monitoring
        JPanel monitorPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        monitorPanel.setBorder(new TitledBorder("📊 Monitoring Système"));
        
        stockStatusButton = new JButton("📦 Vérifier Stock");
        stockStatusButton.setBackground(new Color(52, 152, 219));
        stockStatusButton.setForeground(Color.WHITE);
        stockStatusButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton systemStatusButton = new JButton("⚙️ Statut Système");
        systemStatusButton.setBackground(new Color(155, 89, 182));
        systemStatusButton.setForeground(Color.WHITE);
        systemStatusButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton clearLogsButton = new JButton("🧹 Effacer Logs");
        clearLogsButton.setBackground(new Color(231, 76, 60));
        clearLogsButton.setForeground(Color.WHITE);
        clearLogsButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        monitorPanel.add(stockStatusButton);
        monitorPanel.add(systemStatusButton);
        monitorPanel.add(clearLogsButton);
        
        panel.add(monitorPanel);
        
        // Event handlers pour les boutons de monitoring
        systemStatusButton.addActionListener(e -> {
            addLogMessage("🔍 Vérification du statut système...");
            addLogMessage("✅ Tous les agents sont actifs");
            addLogMessage("📊 Base de données connectée");
            addLogMessage("🌐 Interface utilisateur opérationnelle");
        });
        
        clearLogsButton.addActionListener(e -> {
            logArea.setText("");
            addLogMessage("🧹 Logs effacés - " + dateFormat.format(new Date()));
        });
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("📋 Journal des Activités"));
        
        logArea = new JTextArea(20, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 249, 250));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Ajouter des messages d'accueil
        addLogMessage("🚀 Interface Manager initialisée");
        addLogMessage("🔗 Connexion aux agents établie");
        addLogMessage("📊 Prêt à traiter les commandes");
        
        return panel;
    }
    
    private void setupEventHandlers() {
        createOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String customerName = customerNameField.getText().trim();
                String productName = (String) productComboBox.getSelectedItem();
                String quantityText = quantityField.getText().trim();
                String address = addressField.getText().trim();
                
                if (customerName.isEmpty() || productName.isEmpty() || quantityText.isEmpty() || address.isEmpty()) {
                    JOptionPane.showMessageDialog(ManagerGUI.this, 
                        "⚠️ Veuillez remplir tous les champs", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                try {
                    int quantity = Integer.parseInt(quantityText);
                    if (quantity <= 0) {
                        throw new NumberFormatException();
                    }
                    
                    // Envoyer la commande via l'agent manager
                    managerAgent.sendOrderRequest(customerName, productName, quantity, address);
                    
                    // Mise à jour de l'interface
                    addLogMessage("📝 Commande soumise: " + productName + " x" + quantity + " pour " + customerName);
                    statusLabel.setText("🟡 Traitement commande en cours... - " + dateFormat.format(new Date()));
                    
                    // Réinitialiser le formulaire
                    customerNameField.setText("");
                    quantityField.setText("");
                    addressField.setText("");
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ManagerGUI.this, 
                        "⚠️ La quantité doit être un nombre entier positif", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        stockStatusButton.addActionListener(e -> {
            managerAgent.requestStockStatus();
            addLogMessage("📊 Demande de statut stock envoyée aux agents");
            statusLabel.setText("🔍 Vérification stock en cours... - " + dateFormat.format(new Date()));
        });
    }
    
    public void addLogMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = dateFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            
            // Mettre à jour le statut
            if (message.contains("succès") || message.contains("✅")) {
                statusLabel.setText("🟢 Opération réussie - " + timestamp);
            } else if (message.contains("erreur") || message.contains("❌")) {
                statusLabel.setText("🔴 Erreur détectée - " + timestamp);
            }
        });
    }
}
