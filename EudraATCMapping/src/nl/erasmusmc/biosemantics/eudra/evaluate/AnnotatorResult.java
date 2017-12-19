package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVWriter;

public class AnnotatorResult {
	
	private List<AnnotatorEntry> result;
	private String dataset;
	private String outDir;
	
	public AnnotatorResult(String dataset, String outDir){
		this.dataset = dataset;
		this.outDir = outDir;
		this.result = new ArrayList<AnnotatorEntry>();
	}
	
	public AnnotatorResult(List<AnnotatorEntry> result){
		this.result = new ArrayList<AnnotatorEntry>(result);
	}
	
	public void addEntry(AnnotatorEntry e){
		this.result.add(e);
	}
	
	public void exportResults(String outDir){
		this.outDir = outDir;
		exportResults();
	}
	
	public void exportResults(){
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String fFile = this.outDir + dataset + "_" + timeStamp + ".xlsx";
		 
		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet("Sheet1");
		CreationHelper createHelper = wb.getCreationHelper();
		
		Row row = sheet1.createRow(0);
		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
		XSSFFont font = (XSSFFont) wb.createFont();
		
		font.setBold(true);
		style.setFont(font); 
		
		row.createCell(0).setCellValue("No.");		
		row.createCell(1).setCellValue("Drug");
		row.createCell(2).setCellValue("Annotator 1");
		row.createCell(3).setCellValue("Annotator 2");
		row.createCell(4).setCellValue("Annotator 3");
		row.createCell(5).setCellValue("Match");
		
		row.getCell(0).setCellStyle(style);	// no.
		row.getCell(1).setCellStyle(style); // drug
		row.getCell(2).setCellStyle(style); // annotator 1
		row.getCell(3).setCellStyle(style); // annotator 2
		row.getCell(4).setCellStyle(style); // annotator 3
		row.getCell(5).setCellStyle(style); // match
		
		//System.out.println("No.\tDrug \tAnnotator1\tAnnotator2\tAnnotator3\tMatch");
		
		FileOutputStream fileOut;
		try {
			
			int i = 1;
			int rowIdx = 1;
			for(AnnotatorEntry e: result){
				row = sheet1.createRow(rowIdx++); // new row
				row.createCell(0).setCellValue(i); // no.
				row.createCell(1).setCellValue(e.getDrug()); // drug
				row.getCell(1).setCellStyle(style);
				//e.sort(); // sort two lists
				if (e.getAtc1().equals(e.getAtc2()) && e.getAtc1().equals(e.getAtc3())  ){
					row.createCell(5).setCellValue("TRUE"); // match
				}else{
					row.createCell(5).setCellValue("FALSE"); // match
				}
				
				row.getCell(5).setCellStyle(style);				
								
				int k = 1;
				List<String> found = new ArrayList<String>();				
				List<String> aList1 = e.getAtc1();
				List<String> aList2 = e.getAtc2();
				List<String> aList3 = e.getAtc3();
				//System.out.println(i + ". " + e.getDrug());
				
				// find atc matched
				
				int count = 0;
				
				if (aList1.size() == 0 && aList2.size() == 0 && aList3.size() == 0){
					
					row = sheet1.createRow(rowIdx++); // new row
					row.createCell(1).setCellValue(k);
					row.createCell(2).setCellValue("NULL");
					row.createCell(3).setCellValue("NULL");
					row.createCell(4).setCellValue("NULL");
					row.createCell(5).setCellValue("TRUE");
					
					DataFormat format = wb.createDataFormat();
					XSSFCellStyle decStyle = (XSSFCellStyle) wb.createCellStyle();
					decStyle.setDataFormat(format.getFormat("#.#"));		
					row.createCell(6).setCellValue("1.00");
					row.getCell(6).setCellStyle(decStyle);
					
					
				}else{
					
					int max = aList1.size() > aList2.size()? aList1.size():aList2.size();
					max = max > aList3.size()?max:aList3.size();
					
					for(int idx = 0; idx<max;idx++){
						String atc1 = "";
						 
						
						if (aList1.size() > idx) atc1 = aList1.get(idx);						 
							
						if ( aList2.contains(atc1) && aList3.contains(atc1) ){
							found.add(atc1);							
							row = sheet1.createRow(rowIdx++); // new row
							row.createCell(1).setCellValue(k);
							row.createCell(2).setCellValue(atc1);
							row.createCell(3).setCellValue(atc1);
							row.createCell(4).setCellValue(atc1);
							row.createCell(5).setCellValue("TRUE");								
							count++;								
							k++;
						}							
						 						
					}
					
					aList1.removeAll(found);
					aList2.removeAll(found);
					aList3.removeAll(found);
					max = aList1.size()>aList2.size()?aList1.size():aList2.size();
					max = max > aList3.size()? max: aList3.size();
					
					
					for(int idx=0; idx<max; idx++){
						String atc1, atc2, atc3;
						
						if (aList1.size() > idx ){
							atc1 = aList1.get(idx);
						}else{
							atc1 = "";
						}
						
						if (aList2.size() > idx){
							atc2 = aList2.get(idx);
						}else{
							atc2 = "";
						}
						
						if (aList3.size() > idx){
							atc3 = aList3.get(idx);
						}else{
							atc3 = "";
						}
						
						row = sheet1.createRow(rowIdx++); // new row
						row.createCell(1).setCellValue(k);
						row.createCell(2).setCellValue(atc1);
						row.createCell(3).setCellValue(atc2);
						row.createCell(4).setCellValue(atc3);
						row.createCell(5).setCellValue("FALSE");
						 
						
						k++;
					}
					
					row = sheet1.getRow(rowIdx - k);
					k--;
					if (k > 0){
						
						DataFormat format = wb.createDataFormat();
						XSSFCellStyle decStyle = (XSSFCellStyle) wb.createCellStyle();
						decStyle.setDataFormat(format.getFormat("#.#"));						
						float f = (float) count/ (float) k ;						 
						row.createCell(6).setCellValue(String.format("%.2f", f));
						row.getCell(6).setCellStyle(decStyle);
						
					}
					
				}
				
			
				i++;
			}
			
			
			
			 
			
			fileOut = new FileOutputStream(fFile);
			 
			wb.write(fileOut);
			wb.close();
			fileOut.close();
			System.out.println("Results are exported to " + fFile);
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		
		
	   
	}
	
public void exportResults2Sets(){
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String fFile = this.outDir + dataset + "_" + timeStamp + ".xlsx";
		 
		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet("Sheet1");
		CreationHelper createHelper = wb.getCreationHelper();
		
		Row row = sheet1.createRow(0);
		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
		XSSFFont font = (XSSFFont) wb.createFont();
		
		font.setBold(true);
		style.setFont(font); 
		
		row.createCell(0).setCellValue("No.");		
		row.createCell(1).setCellValue("Drug");
		row.createCell(2).setCellValue("Annotator 1");
		row.createCell(3).setCellValue("Annotator 2");		
		row.createCell(4).setCellValue("Match");
		
		row.getCell(0).setCellStyle(style);	// no.
		row.getCell(1).setCellStyle(style); // drug
		row.getCell(2).setCellStyle(style); // annotator 1
		row.getCell(3).setCellStyle(style); // annotator 2
		row.getCell(4).setCellStyle(style); // 
		
		
		System.out.println("No.\tDrug \tAnnotator1\tAnnotator2\tMatch\tProportion");
		
		FileOutputStream fileOut;
		try {
			
			int i = 1;
			int rowIdx = 1;
			for(AnnotatorEntry e: result){
				row = sheet1.createRow(rowIdx++); // new row
				row.createCell(0).setCellValue(i); // no.
				row.createCell(1).setCellValue(e.getDrug()); // drug
				row.getCell(1).setCellStyle(style);
				//e.sort(); // sort two lists
				if (e.getAtc1().equals(e.getAtc2()) ){
					row.createCell(4).setCellValue("TRUE"); // match
				}else{
					row.createCell(4).setCellValue("FALSE"); // match
				}
				
				row.getCell(4).setCellStyle(style);				
								
				int k = 1;
				List<String> found = new ArrayList<String>();				
				List<String> aList1 = e.getAtc1();
				List<String> aList2 = e.getAtc2();				 
				//System.out.println(i + ". " + e.getDrug());
				
				// find atc matched
				
				int count = 0;
				
				if (aList1.size() == 0 && aList2.size() == 0 ){
					
					row = sheet1.createRow(rowIdx++); // new row
					row.createCell(1).setCellValue(k);
					row.createCell(2).setCellValue("NULL");
					row.createCell(3).setCellValue("NULL");				 
					row.createCell(4).setCellValue("TRUE");
					
					DataFormat format = wb.createDataFormat();
					XSSFCellStyle decStyle = (XSSFCellStyle) wb.createCellStyle();
					decStyle.setDataFormat(format.getFormat("#.#"));		
					row.createCell(5).setCellValue("1.00");
					row.getCell(5).setCellStyle(decStyle);
					
					
				}else{
					
					int max = aList1.size() > aList2.size()? aList1.size():aList2.size();					 
					
					for(int idx = 0; idx<max;idx++){
						String atc1 = "";
						 
						
						if (aList1.size() > idx) atc1 = aList1.get(idx);						 
							
						if ( aList2.contains(atc1)){
							found.add(atc1);							
							row = sheet1.createRow(rowIdx++); // new row
							row.createCell(1).setCellValue(k);
							row.createCell(2).setCellValue(atc1);
							row.createCell(3).setCellValue(atc1);							 
							row.createCell(4).setCellValue("TRUE");								
							count++;								
							k++;
						}							
						 						
					}
					
					aList1.removeAll(found);
					aList2.removeAll(found);					
					max = aList1.size()>aList2.size()?aList1.size():aList2.size();
				 	
					for(int idx=0; idx<max; idx++){
						String atc1, atc2;
						
						if (aList1.size() > idx ){
							atc1 = aList1.get(idx);
						}else{
							atc1 = "";
						}
						
						if (aList2.size() > idx){
							atc2 = aList2.get(idx);
						}else{
							atc2 = "";
						}
						 
						
						row = sheet1.createRow(rowIdx++); // new row
						row.createCell(1).setCellValue(k);
						row.createCell(2).setCellValue(atc1);
						row.createCell(3).setCellValue(atc2);						 
						row.createCell(4).setCellValue("FALSE");
						 
						
						k++;
					}
					
					row = sheet1.getRow(rowIdx - k);
					k--;
					if (k > 0){
						
						DataFormat format = wb.createDataFormat();
						XSSFCellStyle decStyle = (XSSFCellStyle) wb.createCellStyle();
						decStyle.setDataFormat(format.getFormat("#.#"));						
						float f = (float) count/ (float) k ;						 
						row.createCell(5).setCellValue(String.format("%.2f", f));
						row.getCell(5).setCellStyle(decStyle);
						
					}
					
				}
				
			
				i++;
			}
			
			
			
			 
			
			fileOut = new FileOutputStream(fFile);
			 
			wb.write(fileOut);
			wb.close();
			fileOut.close();
			System.out.println("write");
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		
		
	   
	}
	
	private void saveData(String filename, String[] line){
    	
    	CSVWriter writer;
		try {
			
			writer = new CSVWriter(new FileWriter(filename, false), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			writer.writeNext(line);
	    	writer.close();
	    	
		} catch (IOException e) {
			 
			System.out.println("Could not write data to csv file.");
			e.printStackTrace();
		}
    	
    	
	}
	
	private void saveData(String filename, ArrayList<String[]> lines, Boolean append){
	    	
	    	CSVWriter writer;
			try {
				
				writer = new CSVWriter(new FileWriter(filename, append), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
				for(String[] line : lines){
					writer.writeNext(line);
				}
				
		    	writer.close();
		    	
			} catch (IOException e) {
				 
				System.out.println("Could not write data to csv file.");
				e.printStackTrace();
			}
	    	 
		}

}
