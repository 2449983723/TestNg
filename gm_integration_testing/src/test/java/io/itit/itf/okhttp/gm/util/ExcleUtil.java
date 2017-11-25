package io.itit.itf.okhttp.gm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcleUtil {
    private static XSSFSheet excelWSheet;
    private static XSSFWorkbook excelWBook;
    private static XSSFCell cell;
    private static XSSFRow row;
    private static String excelFilePath="C:\\C\\TEST1.xlsx";

    // 设定要设置的Excel的文件路径和Excel 中Sheet名；
    // 在读/写Excel 的时候先要调用此方法
    public static void setExcleFile(String filePath, String sheetName) throws Exception {
        FileInputStream excleFile;
        try {
            // 实例化Excle文件的FileInputStream 对象；
            excleFile = new FileInputStream(filePath);
            // 实例化Excle文件的XSSFWorkbook 对象；
            excelWBook = new XSSFWorkbook(excleFile);
            /*
             * 实例化XSSFSheet 对象，指定ExcelFile中的sheet名称，用于后续对sheet中行和列的操作；
             * 
             */
            excelWSheet = excelWBook.getSheet(sheetName);

        } catch (Exception e) {
            e.getStackTrace();
        }

    }
    /*
     * 读取excle文件指定单元格的函数 ；
     * 
     */

    public static String getCell(int row, int col) throws Exception {

        try {
            // 通过函数参数指定单元格的行号和列，获取指定单元格的对象；
            cell = excelWSheet.getRow(row).getCell(col);
            /*
             * 1.如果单元格的类型为字符串类型，使用getStringCellValue();来获取单元格的内容；
             * 2.如果单元格的类型为数字类型，使用getNumberricCellValue();来获取单元格的内容；
             * 注意：getNumberricCellValue();返回的值为double类型，转为为字符串类型，必须在
             * getNumberricCellValue();前面加上（" "）双引号，用于强制转换为String类型，不加双引号
             * 则会抛错；double类型无法转换为String类型的异常；
             * 
             */
            String cellData = cell.getCellType() == XSSFCell.CELL_TYPE_STRING ? cell.getStringCellValue() + ""
                    : String.valueOf(Math.round(cell.getNumericCellValue()));
            return cellData;
        } catch (Exception e) {
            e.getStackTrace();
            return "";
        }

    }
    /*
     * 在Excle中执行单元格写入数据；
     * 
     * 
     */

    public static void setCellData(int rownum, int colnum, String Result) throws Exception {

        try {
            // 获取excle文件的中行对象；
            row = excelWSheet.getRow(rownum);
            // 如果单元格为空则返回null；
            cell = row.getCell(colnum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) {
                // 当单元格为空是则创建单元格
                // 如果单元格为空无法调用单元格对象的setCellValue方法设定单元格的值 ；
                cell = row.createCell(colnum);
                // 创建单元格和后可以通过调用单元格对象的setCellValue方法设置单元格的值了；
                cell.setCellValue(Result);
            } else {
                // 单元格中有内容，则可以直接调用单元格对象的 setCellValue 方法来设置单元格的值；
                cell.setCellValue(Result);
            }
            FileOutputStream fileout = new FileOutputStream(excelFilePath);
            // 将内容写到Excel文件中 ；
            excelWBook.write(fileout);
            // j调用flush方法强制刷新写入文件；
            fileout.flush();
            fileout.close();
            System.out.println("-----写入成功！------");
        } catch (Exception e) {
            System.out.println(e.getMessage() + e.getStackTrace());
            throw (e);
        }

    }

    public static void TangsetCellData(int rowNum, int colNum, String result) {
        try {
            // 获取行对象
            row = excelWSheet.getRow(rowNum);
            // 如果单元格为空，则返回null
            cell = row.getCell(colNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) {
                // 当单元格对象是Null时，则创建单元格
                // 如果单元格为空，无法直接调用单元格的setCellValue方法设定单元格的值
                cell = row.createCell(rowNum);
                // 调用setCellValue方法设定单元格的值
                cell.setCellValue(result);
            } else {
                // 单元格中有内容，则可以直接调用seCellValue方法设定单元格的值
                cell.setCellValue(result);
            }
            // 实例化写入Excel文件的文件输出流对象
            FileOutputStream fileOut = new FileOutputStream(excelFilePath);
            // 将内容写入Excel中
            excelWBook.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    // 从excel 文件中获取测试数据的静态方法；
    public static Object[][] getTestData(String excelFilePath, String sheetName) throws Exception {
        // 根据参数传入的数据文件路径和文件名称，组合出Excel 数据文件的绝对路径
        // 声明一个文件；
        File file = new File(excelFilePath);
        // 创建FileInputStream 来读取Excel文件内容；
        FileInputStream inputStream = new FileInputStream(file);
        // 声明Workbook 对象；
        Workbook workbook = null;
        // 获取文件名参数的扩展名，判断是“.xlsx” 还是 “.xls” ；
        String fileExtensionName = excelFilePath.substring(excelFilePath.indexOf('.'));
        if (fileExtensionName.equals(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (fileExtensionName.equals(".xls")) {
            workbook = new HSSFWorkbook(inputStream);
        }
        Sheet sheet = workbook.getSheet(sheetName);
        // 获取Excel 数据文件Sheet1 中数据的行数，getLastRowNum 方法获取数据的最后一行的行号，
        // getFistRowNum 获取第一行 最后一行减去第一行就是总行数了
        // 注意excle 的行和列都是从0开始的；
        int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
        // 创建名为records 的List对象来存储从Excel文件中读取的数据；
        List<Object[]> records = new ArrayList<Object[]>();
        // 使用for循环遍历Excel 数据文件的所有数据（除了第一行，第一行为标题行），所以i从1开始而不是从0开始；

        for (int i = 1; i < rowCount + 1; i++) {
            // 使用getRow来获取行对象；
            Row row = sheet.getRow(i);
            /*
             * 声明一个数据，用来存储Excel数据文件每行中的测试用例和数据，数据的大小用getLastCellNum-2
             * 来进行动态声明，实现测试数据个数和数组大小一致，
             * 因为Excel数据文件中的测试数据行的最后一个单元格是测试执行结果，倒数第二个单元格为此测试数据行是否运行的状态位，
             * 所以最后俩列的单元格数据并
             * 不需要传入测试方法中，所以是用getLastCellNum-2的方式去掉每行中的最后俩个单元格数据，计算出需要存储的测试数据个数，
             * 并作为测试数据数组的初始化大小
             * 
             */
            String fields[] = new String[row.getLastCellNum() - 2];

            /*
             * 判断数据行是否要参与测试的执行，Excel 文件的倒数第二列为数据行的状态位， 标记为“y”
             * 表示此数据行要被测试脚本执行，标记为非“y”的数据行均被认为不会参数测试脚本执行，会被跳过；
             */

            if (row.getCell(row.getLastCellNum() - 2).getStringCellValue().equals("y")) {
                for (int j = 0; j < row.getLastCellNum() - 2; j++) {
                    /*
                     * 判断Excel 单元格的内容是数字还是字符， 字符格式调用：
                     * row.getCell(j).getStringCellValue()；
                     * 数字格式调用：row.getCell(j).getNumericCellValue()；
                     */
                    fields[j] = (String) (row.getCell(j).getCellType() == XSSFCell.CELL_TYPE_STRING
                            ? row.getCell(j).getStringCellValue() : "" + row.getCell(j).getNumericCellValue());

                }
                // fields 存储到数组当中；
                records.add(fields);

            }
        }

        /*
         * 定义函数的返回值，即Object[] [] 将存储测试数据的list 转换为一个Object 的二维数组；
         */
        Object[][] results = new Object[records.size()][];
        for (int i = 0; i < records.size(); i++) {
            results[i] = records.get(i);
        }

        return results;

    }

    public static int getLastColumnNum() {
        return excelWSheet.getRow(0).getLastCellNum() - 1;
    }
}