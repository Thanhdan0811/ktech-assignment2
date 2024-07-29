package helper;


import models.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandle {

    private static ArrayList<Product> listProduct = new ArrayList<>();

    FileHandle() {}

    public static ArrayList<Product> readExcel(String filePath) {
        FileInputStream inputStream = null;
        try {
            File fl = new File(filePath);
            inputStream = new FileInputStream(fl);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                int rowNum = row.getRowNum();
                if(rowNum < 5) continue;
                String stt = "";
                String sanpham = "";
                String moTa = "";
                String soLuong = "";
                String giaSP = "";
                for (Cell cell : row) {
                    int cellNum = cell.getColumnIndex();
                    switch (cellNum) {
                        case 0:
                            stt = String.valueOf(cell.getNumericCellValue());
                            break;
                        case 1:
                            sanpham = cell.getStringCellValue();
                            break;
                        case 2:
                            moTa = cell.getStringCellValue();
                            break;
                        case 3:
                            soLuong = String.valueOf(cell.getNumericCellValue());
                            break;
                        case 4:
                            giaSP = String.valueOf(cell.getNumericCellValue());
                            break;
                    }
                }
                if(!stt.equals("0.0")) {
                    listProduct.add(new Product(stt, sanpham, moTa, soLuong, giaSP, null));
                }
            }
            workbook.close();
            inputStream.close();
            return listProduct;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
