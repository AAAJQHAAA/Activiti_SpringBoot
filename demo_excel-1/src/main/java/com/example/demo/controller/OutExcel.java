package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Processing_Result;

@RestController
public class OutExcel {

    private XSSFWorkbook xssfworkbook;

	@RequestMapping(value = "/out", method = {RequestMethod.GET, RequestMethod.POST})
	public void outExcel(
				@PathVariable String path,
				@RequestBody Processing_Result pr) throws Exception{
		InputStream inputStream = 
				OutExcel.class.getClassLoader().getResourceAsStream("/流程测试模版.xlsx");
		FileOutputStream outputStream=new FileOutputStream(new File(path));
	    xssfworkbook = new XSSFWorkbook(inputStream);
	    /**
	     * 读取excel,遍历excel
	     * getNumberOfSheets()获取excel中页数
	     * getSheetAt(i)获取excel中第几页
	     * XSSFSheet对象代表当前页
	     */
	       XSSFSheet sheet = xssfworkbook.getSheetAt(0);
	       //第pr.getId()行每个单元格添加内容
	       XSSFRow row = sheet.createRow(pr.getId()+1);
//	       for(int i=0;i<10;i++) {
//	    	   XSSFCell Cell1=row.createCell(i,CellType.STRING);
//		       Cell1.setCellValue("test");
//	       }
	       XSSFCell Cell0=row.createCell(0,CellType.NUMERIC);
	       Cell0.setCellValue(pr.getId());
	       XSSFCell Cell1=row.createCell(1,CellType.STRING);
	       Cell1.setCellValue(pr.getAction());
	       XSSFCell Cell2=row.createCell(2,CellType.STRING);
	       Cell2.setCellValue(pr.getData());
	       XSSFCell Cell3=row.createCell(3,CellType.STRING);
	       Cell3.setCellValue(pr.getAssignee1());
	       XSSFCell Cell4=row.createCell(4,CellType.STRING);
	       Cell4.setCellValue(pr.getStartUser());
	       XSSFCell Cell5=row.createCell(5,CellType.STRING);
	       Cell5.setCellValue(pr.getAssignee2());
	       XSSFCell Cell6=row.createCell(6,CellType.STRING);
	       Cell6.setCellValue(pr.getVariables1());
	       XSSFCell Cell7=row.createCell(7,CellType.STRING);
	       Cell7.setCellValue(pr.getVariables2());
	       XSSFCell Cell8=row.createCell(8,CellType.STRING);
	       Cell8.setCellValue(pr.getTarget());
	       XSSFCell Cell9=row.createCell(9,CellType.STRING);
	       Cell9.setCellValue(pr.getResult());
           //输出流更新excel
           xssfworkbook.write(outputStream);
           outputStream.flush();
           outputStream.close();
	}
	@RequestMapping(value = "/out2", method = {RequestMethod.GET, RequestMethod.POST})
	public String outExcel2(Processing_Result pr) throws Exception{
		
    	FileInputStream inputStream=new FileInputStream(new File("C://Users//jiqinghua//Desktop//xxx//流程测试模版.xlsx"));
		FileOutputStream outputStream=new FileOutputStream(new File("C://Users//jiqinghua//Desktop//xxx//流程测试模版2.xlsx"));
	    xssfworkbook = new XSSFWorkbook(inputStream);
	    /**
	     * 读取excel,遍历excel
	     * getNumberOfSheets()获取excel中页数
	     * getSheetAt(i)获取excel中第几页
	     * XSSFSheet对象代表当前页
	     */
	       XSSFSheet sheet = xssfworkbook.getSheetAt(0);
	       //第pr.getId()行每个单元格添加内容
	       XSSFRow row = sheet.getRow(pr.getId()+1);
	       XSSFCell Cell0=row.getCell(0);
	       Cell0.setCellValue(pr.getId());
	       XSSFCell Cell1=row.getCell(1);
	       Cell1.setCellValue(pr.getAction());
	       XSSFCell Cell2=row.getCell(2);
	       Cell2.setCellValue(pr.getTarget());
	       XSSFCell Cell3=row.getCell(3);
	       Cell3.setCellValue(pr.getData());
	       XSSFCell Cell4=row.getCell(4);
	       Cell4.setCellValue(pr.getStartUser());
	       XSSFCell Cell5=row.getCell(5);
	       Cell5.setCellValue(pr.getAssignee1());
	       XSSFCell Cell6=row.getCell(6);
	       Cell6.setCellValue(pr.getVariables1());
	       XSSFCell Cell7=row.getCell(7);
	       Cell7.setCellValue(pr.getAssignee2());
	       XSSFCell Cell8=row.getCell(8);
	       Cell8.setCellValue(pr.getVariables2());
	       XSSFCell Cell9=row.getCell(9);
	       Cell9.setCellValue(pr.getResult());
           //输出流更新excel
           xssfworkbook.write(outputStream);
           outputStream.flush();
           outputStream.close();
           return pr.toString();
	}
}
