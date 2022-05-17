package gov.nih.nci.pcdc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.nih.nci.pcdc.Permissible_Values;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PCDCParser {

//    private static final String FILE_NAME = "c:/projects/PCDC/PCDC_Terminology_Composite_2021-05-25.xlsx";
    private static final String FILE_NAME = "c:/projects/PCDC/PCDC_Terminology_v2.xlsx";
    private static final String OUTPUT_FILE_NAME = "c:/projects/PCDC/PCDC_Model";
    private static final String workbookName = "22.05c";
    private static final Integer projectNameCol = 2;
    private static final Integer propertyTypeCol = 14;
    private static final Integer ptCol = 6;
    private static final Integer ptDescCol = 9;
    private static final Integer codeCollectionCol = 11;
    private static final Integer permissibleValueCollectionCol = 12;
    private static final Integer materRowCol = 17;
    public Workbook wb;
    public ArrayList<String> tables = new ArrayList<String>();
    public HashMap<String, ArrayList<Field>> fields = new HashMap<String, ArrayList<Field>>();
    public HashMap<String, Permissible_Values> pvs = new HashMap<String, Permissible_Values>();
    public HashMap<String, String> relationships = new HashMap<String, String>();
    public HashMap<String, ArrayList<String>> props = new HashMap<String, ArrayList<String>>();
	
	public void openFile(String file_name) throws Exception{
		FileInputStream excelFile = new FileInputStream(new File(file_name));
        this.wb = new XSSFWorkbook(excelFile);
        return;
	}
	
	public void parse(boolean isOutput) {
		int p_count = 0;
		int t_count = 0;
		String tableNameComp = "";
    	Permissible_Values current_pv_index = null;
        Sheet datatypeSheet = wb.getSheet(workbookName);

        Iterator<Row> iterator = datatypeSheet.iterator();
        iterator.next(); //Skip the first row

        while (iterator.hasNext()) {
        	
            Row currentRow = iterator.next();
            Cell currentCell = currentRow.getCell(projectNameCol);
            String currentTable = ""; 
            if (currentCell.getCellType() == CellType.STRING) {
            	if (!currentCell.getStringCellValue().equals("")) {
            		currentTable = currentCell.getStringCellValue();
            		if (!tables.contains(currentCell.getStringCellValue())) {
            			if (isOutput) System.out.println("Table: " + currentCell.getStringCellValue());
            			String tableName = currentCell.getStringCellValue(); 
            			tables.add(tableName);
            			fields.put(currentCell.getStringCellValue(), new ArrayList<Field>());
            			if ((!tableName.equals("Program")) && (!tableName.equals("Subject Characteristics Table")) && (!tableName.equals("Study"))) 
            				relationships.put(tableName,"Subject Characteristics Table");
            		}
            	}

            	currentCell = currentRow.getCell(propertyTypeCol);
                if (currentCell == null || (currentCell != null && currentCell.getCellType() == CellType.STRING)) {
                	// The following processes property line
                	if (currentCell != null && !currentCell.getStringCellValue().equals("")) {
                		p_count = 0;
                		t_count = 0;
            			
            			String propertyName = currentRow.getCell(ptCol).getStringCellValue();
            			String propertyDesc = currentRow.getCell(ptDescCol).getStringCellValue();
            			String propertyType = currentRow.getCell(propertyTypeCol).getStringCellValue();
            			
            			
            			//Consolidate permissible values for properties with the same name
                        ArrayList<Field> currentFields;
                        if (fields.get(currentTable) == null) {
                        	currentFields = new ArrayList<Field>();
                        	fields.put(currentTable, currentFields);  	
                        }else {
                        	currentFields = fields.get(currentTable);
                        }
                        
                        Field field = null;
                        Boolean found = false;
                        for (int i=0; i<currentFields.size();i++) {
                        	if (currentFields.get(i).getName().equalsIgnoreCase(propertyName)) {
                        		field = currentFields.get(i);
                        		found = true;
                        		break;
                        	}
                        }
                        
                        if (!found) {
                            field = new Field();
                            currentFields.add(field);
                        }
                        
                        ArrayList<String> currentProps;
                        if (props.get(propertyName) == null) {
                        	currentProps = new ArrayList<String>();
                        	props.put(propertyName,currentProps);  	
                            currentProps.add(currentTable);
                        }else {
                        	currentProps = props.get(propertyName);
                        }

                        field.setName(propertyName);
                        field.setDesc(propertyDesc);
                        field.setType(propertyType);
                        currentProps.add(currentTable);
                        
                        currentCell = currentRow.getCell(codeCollectionCol);
                        String cValue = "EMPTY";
                        if (currentCell != null && currentCell.getCellType() == CellType.STRING) {
                        	cValue = currentCell.getStringCellValue();
                        }
                        if ((propertyType.equalsIgnoreCase("code") || propertyType.equalsIgnoreCase("code || number")) && !cValue.equals("EMPTY")) {
                        	Permissible_Values current_pv = null;
                        	Permissible_Values current_pv1 = new Permissible_Values();
                            if (pvs.get(propertyName) == null) {
                            	current_pv = new Permissible_Values();
                            	current_pv.setName(propertyName);
                            	pvs.put(propertyName, current_pv);
                            } else {
                            	current_pv = pvs.get(propertyName);
                            }
                            
                        	field.setPv(current_pv);
                           
                        	tableNameComp = currentRow.getCell(projectNameCol).getStringCellValue();
                        	String all_pvs = currentRow.getCell(permissibleValueCollectionCol).getStringCellValue();
                        	String[] pvs = all_pvs.split("\\|\\|");
                        	
                        	//current_pv keeps a consolidated list of permissible values 
                        	//current_pv1 keeps a list permissible values for current property. For validation purpose later on
                        	for (String pv: pvs) {
                        		p_count++;
                        		String addpv = pv.stripLeading().stripTrailing();
                        		if (!addpv.stripLeading().equals("") && !current_pv.contains(addpv)) {
                        			current_pv.addValues(addpv);
                        			current_pv.addCodes(addpv);
                        			current_pv.setName(propertyName);
                        		}
                        		if (!addpv.stripLeading().equals("")) {
                        			current_pv1.addValues(addpv);
                        			current_pv1.addCodes(addpv);
                        			current_pv1.setName(propertyName);
                        		}
                        	}
                        	current_pv_index = current_pv1;
                			String masterRowNum = ((Double)currentRow.getCell(materRowCol).getNumericCellValue()).intValue()+"";
                        	if (current_pv_index.getValues().size() < 1) {
                        			System.out.println("\tMaster Row - " + masterRowNum);
                        	}
                        }                	
                	}
                	else {
                		t_count++;

            			String cValue = "";
                        currentCell = currentRow.getCell(6);
                        if (currentCell != null && currentCell.getCellType() == CellType.STRING) {
                        	cValue = currentCell.getStringCellValue();
                        }
                        if (currentCell != null && currentCell.getCellType() == CellType.NUMERIC) {
                        	cValue = ((Double)currentCell.getNumericCellValue()).intValue()+"";
                        }
                        if (current_pv_index == null || !current_pv_index.contains(cValue)) {
                        	String masterRowNum = ((Double)currentRow.getCell(materRowCol).getNumericCellValue()).intValue()+"";
                        	System.out.println("\tMaster Row:" + masterRowNum +" Missing PCDC PT Value:" + cValue);
                        }
                    	if (!tableNameComp.equals(currentRow.getCell(2).getStringCellValue())) {
                        	String masterRowNum = ((Double)currentRow.getCell(materRowCol).getNumericCellValue()).intValue()+"";
                        	System.out.println("\tMaster Row:" + masterRowNum +" Miss Match Table name" + tableNameComp);                    		
                    	}
                	}
                }
            }
        } 
	}

	public void generate_yml(String file_name, boolean isOutput) throws Exception {
		FileWriter myWriter = new FileWriter(file_name);
		myWriter.write("Nodes:\n");
		for (Map.Entry<String, ArrayList<Field>> set :
            this.fields.entrySet()) {
			myWriter.write("  " + set.getKey() + ":\n");
			ArrayList<Field> fields = set.getValue();
			myWriter.write("    Category:\n");
			myWriter.write("    Props:\n");
			for (int i=0; i < fields.size(); i++) {
				myWriter.write("      - "+fields.get(i).getName() + "\n");
			}
		}
		myWriter.write("Relationships:\n");
		myWriter.write("  of_programs:\n");
		myWriter.write("    Mul: many_to_one\n");
		myWriter.write("    Ends:\n");
		myWriter.write("      - Src: Subject Characteristics Table\n");
		myWriter.write("        Dst: Program\n");
		myWriter.write("      - Src: Study\n");
		myWriter.write("        Dst: Program\n");
		myWriter.write("      - Src: Subject Characteristics Table\n");
		myWriter.write("        Dst: Study\n");
		myWriter.write("  of_subject:\n");
		myWriter.write("    Mul: many_to_one\n");
		myWriter.write("    Ends:\n");
		for (Map.Entry<String, String> set :
            relationships.entrySet()) {
			if (isOutput) System.out.println(set.getKey() + set.getValue());
			if (set.getValue().equals("Subject Characteristics Table")) {
				myWriter.write("      - Src: " + set.getKey()+ "\n");
				myWriter.write("        Dst: Subject Characteristics Table\n");
			}
			
		}
		myWriter.close();
		
	}
	
	public void generate_desc_yml(String file_name, boolean isOutput) throws Exception {
		
		if (isOutput) {
			System.out.println(props.size());
			for(String a: props.keySet()) {
				System.out.println(a);
			}
		}
		
		FileWriter myWriter = new FileWriter(file_name);
	    HashMap<String, Boolean> processed = new HashMap<String, Boolean>();

		myWriter.write("PropDefinitions:\n");
		for (Map.Entry<String, ArrayList<Field>> set :
            this.fields.entrySet()) {
			ArrayList<Field> fields = set.getValue();
			for (int i=0; i < fields.size(); i++) {
				if (props.get(fields.get(i).getName()).size() == 1)
					myWriter.write("   #Property of "+ props.get(fields.get(i).getName()).get(0) + ":\n");
				else {
					if (processed.get(fields.get(i).getName()) == null) {
						processed.put(fields.get(i).getName(), true);
						myWriter.write("   #Property of "+ props.get(fields.get(i).getName()).get(0));
						for (int k = 1; k < props.get(fields.get(i).getName()).size(); k++)
							myWriter.write("," + props.get(fields.get(i).getName()).get(k) );
						myWriter.write("\n");
					}
					else {
						continue;
					}
				}
				myWriter.write("   "+fields.get(i).getName() + ":\n");
				myWriter.write("     Desc: "+fields.get(i).getDesc() + "\n");
				myWriter.write("     Src: NA\n");
				String type = fields.get(i).getType();
				if (type.equals("string") || type.equals("number") || type.equals("Text")) {
					myWriter.write("     Type: " + type + "\n");
				} else {
					myWriter.write("     Type:\n");
					Permissible_Values pv = fields.get(i).getPv();
					ArrayList<String> codes = pv.getCodes();
					for (int j=0; j<codes.size();j++) {
						if (j > 20) break;
						myWriter.write("        - \"" + codes.get(j)+"\"\n" );
					}
				}
				myWriter.write("     Req: "+fields.get(i).getIsRequired() + "\n");
				myWriter.write("     Private: "+fields.get(i).getIsVisible() + "\n");
				
			}
		}
		myWriter.close();
		
	}
	
	public void generate_one_yml(String file_name, boolean isOutput) throws Exception {
		FileWriter myWriter = new FileWriter(file_name);
		myWriter.write("Nodes:\n");
		for (Map.Entry<String, ArrayList<Field>> set :
            this.fields.entrySet()) {
			myWriter.write("  " + set.getKey() + ":\n");
			ArrayList<Field> fields = set.getValue();
			myWriter.write("    Category:\n");
			myWriter.write("    Props:\n");
			for (int i=0; i < fields.size(); i++) {
				myWriter.write("      - "+fields.get(i).getName() + "\n");
				myWriter.write("        Desc:"+fields.get(i).getDesc() + "\n");
				myWriter.write("        Src: NA\n");
				String type = fields.get(i).getType();
				if (type.equals("string") || type.equals("number") || type.equals("Text")) {
					myWriter.write("        Type: " + type + "\n");
				} else {
					myWriter.write("        Type:\n");
					Permissible_Values pv = fields.get(i).getPv();
					ArrayList<String> codes = pv.getCodes();
					for (int j=0; j<codes.size();j++) {
//						if (j > 20) break;
						myWriter.write("           - \"" + codes.get(j)+"\"\n" );
					}
				}
				myWriter.write("        Req: "+fields.get(i).getIsRequired() + "\n");
				myWriter.write("        Private: "+fields.get(i).getIsVisible() + "\n\n");
			}
		}
		myWriter.write("Relationships:\n");
		myWriter.write("  of_programs:\n");
		myWriter.write("    Mul: many_to_one\n");
		myWriter.write("    Ends:\n");
		myWriter.write("      - Src: Subject Characteristics Table\n");
		myWriter.write("        Dst: Program\n");
		myWriter.write("      - Src: Study\n");
		myWriter.write("        Dst: Program\n");
		myWriter.write("      - Src: Subject Characteristics Table\n");
		myWriter.write("        Dst: Study\n");
		myWriter.write("  of_subject:\n");
		myWriter.write("    Mul: many_to_one\n");
		myWriter.write("    Ends:\n");
		for (Map.Entry<String, String> set :
            relationships.entrySet()) {
			if (isOutput) System.out.println(set.getKey() + set.getValue());
			if (set.getValue().equals("Subject Characteristics Table")) {
				myWriter.write("      - Src: " + set.getKey()+ "\n");
				myWriter.write("        Dst: Subject Characteristics Table\n");
			}
			
		}
		myWriter.close();
		
	}

	public static void main(String[] args) throws Exception {
		
		PCDCParser pcdc = new PCDCParser();
		pcdc.openFile(FILE_NAME);
		pcdc.parse(false);
		pcdc.generate_yml(OUTPUT_FILE_NAME+".yml",false);
		pcdc.generate_desc_yml(OUTPUT_FILE_NAME+"_Props.yml", false);
		pcdc.generate_one_yml(FILE_NAME+"_One_File.yml",false);


        
	};
	

}
