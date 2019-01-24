package com.example.demo.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {
    private final static String xls = "xls";
    private final static String xlsx = "xlsx";

    /**
     * 读入excel文件，解析后返回
     * @param file
     * @throws IOException
     */
    public static List<String[]> readExcel(MultipartFile file) throws IOException{
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        List<String[]> list = new ArrayList<String[]>();
        if(workbook != null){
            for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++){
                //获得当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if(sheet == null){
                    continue;
                }
                //获得当前sheet的开始行
                int firstRowNum  = sheet.getFirstRowNum();
                //获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                //循环除了第一行的所有行
                for(int rowNum = firstRowNum+1;rowNum <= lastRowNum;rowNum++){
                    //获得当前行
                    Row row = sheet.getRow(rowNum);
                    if(row == null){
                        continue;
                    }
                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    //循环当前行
                    for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++){
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
            workbook.close();
        }
        return list;
    }
    public static void checkFile(MultipartFile file) throws IOException{
        //判断文件是否存在
        if(null == file){
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = file.getOriginalFilename();
        //判断文件是否是excel文件
        if(!fileName.endsWith(xls) && !fileName.endsWith(xlsx)){
            throw new IOException(fileName + "不是excel文件");
        }
    }
    public static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if(fileName.endsWith(xls)){
                //2003
                workbook = new HSSFWorkbook(is);
            }else if(fileName.endsWith(xlsx)){
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
        }
        return workbook;
    }
    public static String getCellValue(Cell cell){
        String cellValue = "";
        if(cell == null){
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()){
            case Cell.CELL_TYPE_NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue()).replace('\"','"');
                break;
            case Cell.CELL_TYPE_BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case Cell.CELL_TYPE_BLANK: //空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    /**
     * 生成excel表格
     * @param list  表格数据
     * @param sheetName  表格名称
     * @param columnNames  表头名称
     * @return
     */
    public static HSSFWorkbook createWorkBook(List<String[]> list, String sheetName, String[] columnNames) {
        //创建一个excel文件
        HSSFWorkbook wb = new HSSFWorkbook();
        //创建一个表名称
        HSSFSheet sheet = wb.createSheet(sheetName);

        //设置每列的宽度
        for (int i = 0; i < list.get(0).length; i++) {
            sheet.setColumnWidth(i,15*256);
        }

        //设置标题字体
        Font fontTitle = wb.createFont();
        fontTitle.setFontHeightInPoints((short) 12); //字体大小
        fontTitle.setColor(HSSFColor.BLACK.index); //字体颜色
        fontTitle.setFontName("DengXian"); //字体
        fontTitle.setBold(true); //粗体显示

        //设置标题单元格类型
        CellStyle cellStyleTitle = wb.createCellStyle();
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//        cellStyleTitle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//        cellStyleTitle.setAlignment(CellStyle.ALIGN_CENTER); //水平布局：居中
//        cellStyleTitle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cellStyleTitle.setWrapText(false);//设置自动换行

//        cellStyleTitle.setBorderBottom(CellStyle.BORDER_THIN); //下边框
//        cellStyleTitle.setBorderLeft(CellStyle.BORDER_THIN);//左边框
//        cellStyleTitle.setBorderTop(CellStyle.BORDER_THIN);//上边框
//        cellStyleTitle.setBorderRight(CellStyle.BORDER_THIN);//右边框
        cellStyleTitle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleTitle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleTitle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleTitle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        //合并单元格
        sheet.addMergedRegion(new CellRangeAddress(0,0,3,4));
        sheet.addMergedRegion(new CellRangeAddress(0,0,5,6));
        sheet.addMergedRegion(new CellRangeAddress(0,0,7,8));

        //创建表头
        HSSFRow row = sheet.createRow(0);
        //创建单元格设置表头
        HSSFCell cell = null;
        //创建表头
        for (int i = 0; i < columnNames.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(columnNames[i]);
            cell.setCellStyle(cellStyleTitle);
        }

        //设置内容字体
        Font fontData = wb.createFont();
        fontData.setFontHeightInPoints((short) 12); //字体大小
        fontData.setColor(Font.COLOR_NORMAL); //字体颜色
        fontData.setFontName("DengXing"); //字体

        //设置内容单元格类型
        CellStyle cellStyleDataOdd = wb.createCellStyle();
        cellStyleDataOdd.setFont(fontData);
        cellStyleDataOdd.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//        cellStyleDataOdd.setFillPattern(CellStyle.SOLID_FOREGROUND);
//        cellStyleDataOdd.setAlignment(CellStyle.ALIGN_LEFT); //水平布局：居中
//        cellStyleDataOdd.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        cellStyleDataOdd.setWrapText(false);

//        cellStyleDataOdd.setBorderBottom(CellStyle.BORDER_THIN); //下边框
//        cellStyleDataOdd.setBorderLeft(CellStyle.BORDER_THIN);//左边框
//        cellStyleDataOdd.setBorderTop(CellStyle.BORDER_THIN);//上边框
//        cellStyleDataOdd.setBorderRight(CellStyle.BORDER_THIN);//右边框
        cellStyleDataOdd.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleDataOdd.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleDataOdd.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleDataOdd.setRightBorderColor(IndexedColors.BLACK.getIndex());

        //创建内容
        for (int i = 0; i < list.size(); i++) {
            HSSFRow rowContent = sheet.createRow(i+1);
            HSSFCell cellContent = null;
            for (int j = 0; j < list.get(i).length; j++) {
                cellContent = rowContent.createCell(j);
                cellContent.setCellStyle(cellStyleDataOdd);
                cellContent.setCellValue(list.get(i)[j]);
            }
            if(i+1 == list.size()){
                rowContent.setHeight((short) (100*20));
            }
        }
        sheet.addMergedRegion(new CellRangeAddress(list.size(),list.size(),1,9));
        sheet.addMergedRegion(new CellRangeAddress(0,1,9,9));

        return wb;
    }
}
