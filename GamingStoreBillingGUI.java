import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GamingStoreBillingGUI {

    static final String DB_URL = "jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "dbms";

    JFrame frame;
    JTable table;
    DefaultTableModel model;
    JTextField searchField;

    public GamingStoreBillingGUI() {
        frame = new JFrame("Gaming Store Billing Software");

        model = new DefaultTableModel(
                new String[]{"ID", "Product Name", "Type", "Quantity", "Price"}, 0
        );

        table = new JTable(model);
        searchField = new JTextField(15);

        JButton load = new JButton("Load");
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton search = new JButton("Search");
        JButton sell = new JButton("Sell");
        JButton clear = new JButton("Clear");

        JPanel top = new JPanel();
        top.add(new JLabel("Search by ID / Name / Type:"));
        top.add(searchField);
        top.add(search);

        JPanel bottom = new JPanel();
        bottom.add(load);
        bottom.add(add);
        bottom.add(update);
        bottom.add(delete);
        bottom.add(sell);
        bottom.add(clear);

        frame.add(top, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setSize(950, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        load.addActionListener(e -> loadData());
        add.addActionListener(e -> addProduct());
        update.addActionListener(e -> updateProduct());
        delete.addActionListener(e -> deleteProduct());
        search.addActionListener(e -> searchProduct());
        sell.addActionListener(e -> sellProduct());
        clear.addActionListener(e -> model.setRowCount(0));

        loadData();
    }

    Connection getCon() throws Exception {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    void loadData() {
        model.setRowCount(0);
        try (Connection c = getCon();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM gaming_accessories")) {

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getInt("id"),
                        r.getString("product_name"),
                        r.getString("type"),
                        r.getInt("quantity"),
                        r.getDouble("price")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Load Error: " + e.getMessage());
        }
    }

    void addProduct() {
        try (Connection c = getCon()) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO gaming_accessories (id, product_name, type, quantity, price) VALUES (?, ?, ?, ?, ?)"
            );

            ps.setInt(1, Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter ID")));
            ps.setString(2, JOptionPane.showInputDialog(frame, "Enter Product Name"));
            ps.setString(3, JOptionPane.showInputDialog(frame, "Enter Type"));
            ps.setInt(4, Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter Quantity")));
            ps.setDouble(5, Double.parseDouble(JOptionPane.showInputDialog(frame, "Enter Price")));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Product added successfully");
            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Add Error: " + e.getMessage());
        }
    }

    void updateProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Select a row first");
            return;
        }

        try (Connection c = getCon()) {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());

            String currentName = model.getValueAt(row, 1).toString();
            String currentType = model.getValueAt(row, 2).toString();
            String currentQty = model.getValueAt(row, 3).toString();
            String currentPrice = model.getValueAt(row, 4).toString();

            String newName = JOptionPane.showInputDialog(frame, "Enter Product Name", currentName);
            if (newName == null) return;

            String newType = JOptionPane.showInputDialog(frame, "Enter Type", currentType);
            if (newType == null) return;

            String newQty = JOptionPane.showInputDialog(frame, "Enter Quantity", currentQty);
            if (newQty == null) return;

            String newPrice = JOptionPane.showInputDialog(frame, "Enter Price", currentPrice);
            if (newPrice == null) return;

            PreparedStatement ps = c.prepareStatement(
                    "UPDATE gaming_accessories SET product_name=?, type=?, quantity=?, price=? WHERE id=?"
            );

            ps.setString(1, newName);
            ps.setString(2, newType);
            ps.setInt(3, Integer.parseInt(newQty));
            ps.setDouble(4, Double.parseDouble(newPrice));
            ps.setInt(5, id);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Product updated successfully");
            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Update Error: " + e.getMessage());
        }
    }

    void deleteProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Select a row first");
            return;
        }

        try (Connection c = getCon()) {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());

            PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM gaming_accessories WHERE id=?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Product deleted successfully");
            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Delete Error: " + e.getMessage());
        }
    }

    void searchProduct() {
        model.setRowCount(0);

        try (Connection c = getCon()) {
            String key = searchField.getText().trim();

            if (key.isEmpty()) {
                loadData();
                return;
            }

            PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM gaming_accessories " +
                    "WHERE CAST(id AS CHAR) LIKE ? OR product_name LIKE ? OR type LIKE ?"
            );

            String searchKey = "%" + key + "%";
            ps.setString(1, searchKey);
            ps.setString(2, searchKey);
            ps.setString(3, searchKey);

            ResultSet r = ps.executeQuery();

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getInt("id"),
                        r.getString("product_name"),
                        r.getString("type"),
                        r.getInt("quantity"),
                        r.getDouble("price")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Search Error: " + e.getMessage());
        }
    }

    void sellProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Select a row first");
            return;
        }

        try (Connection c = getCon()) {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            int qtyToSell = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter quantity to sell"));

            PreparedStatement ps1 = c.prepareStatement(
                    "SELECT product_name, price, quantity FROM gaming_accessories WHERE id=?"
            );
            ps1.setInt(1, id);

            ResultSet r = ps1.executeQuery();

            if (r.next()) {
                String name = r.getString("product_name");
                double price = r.getDouble("price");
                int stock = r.getInt("quantity");

                if (qtyToSell > stock) {
                    JOptionPane.showMessageDialog(frame, "Not enough stock. Available stock: " + stock);
                    return;
                }

                int newStock = stock - qtyToSell;
                double total = price * qtyToSell;

                PreparedStatement ps2 = c.prepareStatement(
                        "UPDATE gaming_accessories SET quantity=? WHERE id=?"
                );
                ps2.setInt(1, newStock);
                ps2.setInt(2, id);
                ps2.executeUpdate();

                PreparedStatement ps3 = c.prepareStatement(
                        "INSERT INTO sales(product_id, product_name, unit_price, sold_quantity, total_amount) VALUES (?, ?, ?, ?, ?)"
                );
                ps3.setInt(1, id);
                ps3.setString(2, name);
                ps3.setDouble(3, price);
                ps3.setInt(4, qtyToSell);
                ps3.setDouble(5, total);
                ps3.executeUpdate();

                JOptionPane.showMessageDialog(frame,
                        "========== BILL ==========\n" +
                        "Product ID   : " + id + "\n" +
                        "Product Name : " + name + "\n" +
                        "Unit Price   : " + price + "\n" +
                        "Sold Qty     : " + qtyToSell + "\n" +
                        "Total Amount : " + total + "\n" +
                        "Stock Left   : " + newStock + "\n" +
                        "==========================");

                loadData();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Sell Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GamingStoreBillingGUI::new);
    }
}