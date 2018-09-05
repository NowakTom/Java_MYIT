package statuses;


import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import au.com.bytecode.opencsv.CSVWriter;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Vector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


class MainFrame extends JFrame implements ActionListener
{
//Panel, u³o¿enie przycisków 
  JButton loadButton ; 
  JButton saveButton ; 
  JButton selectButton1 ;
  JButton selectButton2;
  JButton selectButton3;
  JTextField jsonPath;
  JTextField csvPath;
  JFileChooser openJsonFile;
  JFileChooser openCsvDir;

  MainFrame(String title) 
  {
    super( title );
    setLayout( null );

    loadButton = new JButton("Load .json");
    loadButton.setBounds(new Rectangle(410,20,120,25));
    
    saveButton = new JButton("Save .csv Ex.1"); 
    saveButton.setBounds(new Rectangle(410,50,120,25));
    
    selectButton1 = new JButton("Select Ex.1"); 
    selectButton1.setBounds(new Rectangle(230,80,120,25));
    
    selectButton2 = new JButton("Select Ex.2");
    selectButton2.setBounds(new Rectangle(230,110,120,25));
    
    selectButton3 = new JButton("Select Ex.3"); 
    selectButton3.setBounds(new Rectangle(230,140,120,25));
 
    jsonPath = new JTextField();
    jsonPath.setBounds(new Rectangle(30,20,370,25));
    jsonPath.setText(".json file path");
    jsonPath.setEditable(false);
    
    csvPath = new JTextField();
    csvPath.setBounds(new Rectangle(30,50,370,25));
    csvPath.setText(".csv directory path");
    csvPath.setEditable(false);
    
    openJsonFile = new JFileChooser();
    openCsvDir = new JFileChooser();

    loadButton.addActionListener( this ); 
    saveButton.addActionListener( this ); 
    selectButton1.addActionListener( this ); 
    selectButton2.addActionListener( this );
    selectButton3.addActionListener( this );

    add( loadButton );      
    add( saveButton );
    add( selectButton1);
    add( selectButton2);
    add( selectButton3);
    add(jsonPath);
    add(csvPath);
    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );   
  }
  
  //Obs³uga zdarzeñ
  public void actionPerformed( ActionEvent evt)
  {
	  statuses_parse obj = new statuses_parse();
		Object source = evt.getSource();
		
		//Przycisk wczytujacy dane z pliku json do bazy
		if(source == loadButton) {
			//Chooser do pliku .json
			loadButton.setEnabled(false);
			openJsonFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
			openJsonFile.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public boolean accept(java.io.File file) {
					if (file.getName().endsWith(".json"))
						return true;
					if (file.isDirectory())
						return true;
					return false;
				}
				public String getDescription()
				{
					return "JSON files (.*json)";
				}
			});
			
			int returnVal = openJsonFile.showOpenDialog(MainFrame.this);
			openJsonFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = openJsonFile.getSelectedFile();
				jsonPath.setText(file.getAbsolutePath());
			}
		//Tworzenie bazy i tabeli
		obj.createNewTable();
		
		    try
	    {
		File f = openJsonFile.getSelectedFile();
		//£adowanie danych do tabeli z trybem "check"
		Boolean status = obj.parse_json(f, "check");
	    if(status == true)
	    	{
	    	//Czyszczenie tabeli statusy
			obj.clear();
			//£adowanie danych do tabeli z trybem "insert"
	    	obj.parse_json(f, "insert");
	    	JOptionPane.showMessageDialog(null, "File " + openJsonFile.getSelectedFile().getName() + " loaded and inserted into sample.db","Info", JOptionPane.INFORMATION_MESSAGE);
	    	};
	    loadButton.setEnabled(true);
	  } catch (Exception e) {
	      System.out.println(e.getMessage());
	      loadButton.setEnabled(true);
	  }}
		//Przycisk tworz¹cy plik .csv
		else if(source == saveButton) {
			//Chooser do lokalizacji zapisu pliku .csv
			openCsvDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			openCsvDir.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public boolean accept(java.io.File file) {
					if (file.getName().endsWith(".json"))
						return true;
					if (file.isDirectory())
						return true;
					return false;
				}
				public String getDescription()
				{
					return "Directories";
				}
			});
			
			int returnVal = openCsvDir.showOpenDialog(MainFrame.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = openCsvDir.getSelectedFile();
				csvPath.setText(file.getAbsolutePath());
			}
			try	{
				//Tworzenie pliku .csv
				if (!openCsvDir.getSelectedFile().equals(null))
				{
				obj.csvCreator(openCsvDir.getSelectedFile(), obj.SQLs(1));
				JOptionPane.showMessageDialog(null, "out.csv file created in: " + csvPath.getText(), "Info", JOptionPane.INFORMATION_MESSAGE);
				}
			}  catch (Exception e) {
			      System.out.println(e.getMessage());
			  }
		}
		//Zapytanie sql do zadania 1
		else if(source == selectButton1) {
			obj.select(obj.SQLs(1));
		}
		//Zapytanie sql do zadania 2
		else if(source == selectButton2) {
			obj.select(obj.SQLs(2));
		}
		//Zapytanie sql do zadania 3
		else if(source == selectButton3) {
			obj.select(obj.SQLs(3));
		}
  }
}

//Zapytania sql do zadañ
public class statuses_parse
{
	
	public String SQLs(int exercise)
	{
    	String sql = "";
    	if (exercise == 1)
    	{
    	sql = "select * \n"
    		+ "from statusy \n"
    		+ "where date(kontakt_ts) >= '2017-07-01' \n"
    		+ "order by klient_id, kontakt_ts";
    	}
    	else if (exercise == 2)
    	{
    	sql = "select klient_id, status, kontakt_ts \n"
    		+ "from statusy wszystkie join \n"
    			+ "(select klient_id as wybrani_klient_id, max(kontakt_ts) as wybrani_kontakt_ts \n"
    			+ "from statusy \n"
    			+ "group by klient_id \n"
    			+ "having count(*)>=3) wybrani \n"
    		+ "on wszystkie.klient_id=wybrani.wybrani_klient_id and wszystkie.kontakt_ts=wybrani.wybrani_kontakt_ts";
    	}
    	else if (exercise == 3)
    	{
    	sql= "select date(kontakt_ts) as data, \n"
    		+ "sum(case when status='zainteresowany' then 1 else 0 end) as sukcesy, \n"
    		+ "sum(case when status='niezainteresowany' then 1 else 0 end) as utraty, \n"
    		+ "sum(case when status='poczta_glosowa' or status ='nie_ma_w_domu' then 1 else 0 end) as do_ponowienia \n"
    		+ "from \n"
    			+ "(select klient_id, status, kontakt_ts from statusy wszystkie join \n"
    			+" (select klient_id as wybrani_klient_id, max(kontakt_ts) as wybrani_kontakt_ts \n"
    			+ "from statusy group by klient_id ) wybrani \n"
    			+ "on wszystkie.klient_id=wybrani.wybrani_klient_id and wszystkie.kontakt_ts=wybrani.wybrani_kontakt_ts) nowa_tabela \n"
    		+ "group by date(kontakt_ts)";
    			}
		return sql;
	}
	//Tabela na potrzeby prezentacji wyników zapytañ select
	public static DefaultTableModel buildTableModel(ResultSet rs)
	        throws SQLException {

	    ResultSetMetaData metaData = rs.getMetaData();

	    Vector<String> columnNames = new Vector<String>();
	    int columnCount = metaData.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
	        columnNames.add(metaData.getColumnName(column));
	    }

	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    while (rs.next()) {
	        Vector<Object> vector = new Vector<Object>();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	            vector.add(rs.getObject(columnIndex));
	        }
	        data.add(vector);
	    }

	    return new DefaultTableModel(data, columnNames);

	}
	//Rozk³ad pliku json na zmienne i insert do bazy
	public boolean parse_json (File f, String mode) {
		Boolean status = false;
		try {
	    if (f.exists()){
	        InputStream is = new FileInputStream(f);
	        String jsonTxt = IOUtils.toString(is, "UTF-8");       
	        JSONArray values = new JSONArray(jsonTxt);
            int kontakt_id;
            int klient_id;
            int pracownik_id;
            String statusy;
            String kontakt_ts;

	        for (int i = 0; i < values.length(); i++) {
	            
	            JSONObject zamowienia = values.getJSONObject(i); 

	            kontakt_id = zamowienia.getInt("kontakt_id");
	            klient_id = zamowienia.getInt("klient_id");
	            pracownik_id = zamowienia.getInt("pracownik_id");
	            statusy = zamowienia.getString("status");
	            kontakt_ts = zamowienia.getString("kontakt_ts");
	            if (mode == "insert")
	            {
	            insert(kontakt_id, klient_id, pracownik_id, statusy, kontakt_ts);
	            }
	          }
	        status = true;
	    }
	  } catch (IOException | JSONException e) {
		  if (mode != "insert")
		  {
		  JOptionPane.showMessageDialog(null, "Invalid .json file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		  }
	      status = false;
	  }
		return status;
	}
	//Metoda po³¹czenia do bazy
    private Connection connect() {

    	String url = "jdbc:sqlite:statuses.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    //Tworzenie tabeli statusy i bazy statuses.db je¿eli nie istnieje
    public void createNewTable() {

        String sql = "CREATE TABLE IF NOT EXISTS statusy (\n"
                + "	kontakt_id integer PRIMARY KEY,\n"
                + "	klient_id integer NOT NULL,\n"
                + "	pracownik_id integer NOT NULL,\n"
                + "	status text NOT NULL,\n"
                + "	kontakt_ts text NOT NULL\n"
                + ");";
        
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(null, "SQl error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);;
        } 
    }
    //Metoda umo¿liwiaj¹ca insert do bazy
    public void insert(int kontakt_id, int klient_id, int pracownik_id, String status, String kontakt_ts) {
    	
    	String sql = "INSERT INTO statusy(kontakt_id, klient_id, pracownik_id, status, kontakt_ts) VALUES(?,?,?,?,?)";
 
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kontakt_id);
            pstmt.setInt(2, klient_id);
            pstmt.setInt(3, pracownik_id);
            pstmt.setString(4, status);
            pstmt.setString(5, kontakt_ts);
            pstmt.executeUpdate();
        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(null, "SQl error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //Metoda do zapytañ select
    public void select(String sql) {
    	
    	try (Connection conn = this.connect();
        		 Statement stmt  = conn.createStatement();
                ResultSet rs    = stmt.executeQuery(sql)){
    		
    	    JTable table = new JTable(buildTableModel(rs));

    	    JOptionPane.showMessageDialog(null, new JScrollPane(table), "Query result", JOptionPane.INFORMATION_MESSAGE);
               
        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(null, "SQl error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //Tworzenie pliku .csv
    public void csvCreator(File dir, String sql) {
        String FILENAME = "out.csv";
        File directoryDownload = dir;
        File logDir = new File(directoryDownload, FILENAME);
        try {
            logDir.createNewFile();
            CSVWriter csvWriter = new CSVWriter(new FileWriter(logDir), '|', CSVWriter.NO_QUOTE_CHARACTER);
            Connection conn = this.connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            String colNames[] = {rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3),
            		rsmd.getColumnName(4), rsmd.getColumnName(5)};
            csvWriter.writeNext(colNames);
            while (rs.next()) {
                String arrStr[] = { rs.getString(1) , rs.getString(2) ,
                		rs.getString(3), rs.getString(4), rs.getString(5)};
                csvWriter.writeNext(arrStr);
            }
            csvWriter.close();
            rs.close();
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null, "csv creator error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}
        }
    //Metoda czyszcz¹ca dane z tabeli statusy
    public void clear() {
    	
    	String sql = "DELETE from statusy";
 
    	try (Connection conn = this.connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(null, "SQl error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
//Metoda main
  public static void main ( String[] args ) throws Exception
  {
	    MainFrame frm = new MainFrame("Json Parser");

	    frm.setSize( new Dimension(580,230) );     
	    frm.setVisible( true );
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension windowSize = frm.getSize();
	    int windowX = Math.max(0,  (screenSize.width - windowSize.width) / 2);
	    int windowY = Math.max(0,  (screenSize.height - windowSize.height) / 2);
	    frm.setLocation(windowX, windowY);
  }
}