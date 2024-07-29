import helper.DB;
import helper.FileHandle;
import helper.GenerateId;
import helper.HashPass;
import models.Product;
import models.UserAdmin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Connection connect = null;
    private static UserAdmin currentUser = null;
//    private static ArrayList<Product> listProduct = new ArrayList<>();

    public static void main(String[] args) {
        DB db = new DB();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // check login;
        while (true) {
            System.out.println("========");
            loginMain(db, preparedStatement, resultSet);
            if(currentUser != null) break;

        }

        String luachon;

        if(Objects.equals(currentUser.getRoleId(), "0")) {
            while (true) {
                menuUAdmin();
                luachon = scanner.nextLine().trim();
                if(luachon.equals("0")) {
                    currentUser = null;
                    return;
                }
                switch (luachon) {
                    case "1":
                        addUser(db, preparedStatement, resultSet);
                        break;
                    case "2":
                        editUser(db, preparedStatement, resultSet);
                        break;
                    case "3":
                        deleteUser(db, preparedStatement, resultSet);
                        break;
                    case "4":
                        addWarehouse(db, preparedStatement, resultSet);
                        break;
                    case "5":
                        editWarehouse(db, preparedStatement, resultSet);
                        break;
                    case "6":
                        deleteWarehouse(db, preparedStatement, resultSet);
                        break;
                    case "7":
                        showUsers(db, preparedStatement, resultSet);
                        break;
                    case "8":
                        showWarehouses(db, preparedStatement, resultSet);
                        break;
                    case "9":
                        nhapSpVaoKho(null, db, preparedStatement, resultSet);
                        break;
                    default:
                        System.out.println("Lựa chọn chưa phù hợp.");
                        break;
                }
            }
        }

        if(Objects.equals(currentUser.getRoleId(), "1")) {
            System.out.println();
            while (true) {
                menuUser();
                luachon = scanner.nextLine().trim();
                if(luachon.equals("0")) {
                    currentUser = null;
                    return;
                }
                switch (luachon) {
                    case "1":
                        showProductInWarehouse(db, preparedStatement, resultSet, currentUser.getWarehouseId());
                        break;
                    case "2":
                        addProductToWarehouse(db, preparedStatement, resultSet, currentUser.getWarehouseId());
                        break;
                    default:
                        System.out.println("Lựa chọn chưa phù hợp.");
                        break;
                }
            }
        }


    }

    public static void menuUser() {
        System.out.println("Nhập lựa chọn : ");
        System.out.println("1. Xem sản phẩm trong kho.");
        System.out.println("2. Thêm sản phẩm từ file excel.");
        System.out.println("0. Để thoát");
    }

    public static void menuUAdmin() {
        System.out.println("Nhập lựa chọn : ");
        System.out.println("1. Thêm user");
        System.out.println("2. Sửa user");
        System.out.println("3. Xóa user");
        System.out.println("4. Thêm kho");
        System.out.println("5. Sửa kho");
        System.out.println("6. Xóa kho");
        System.out.println("7. Xem danh sách users.");
        System.out.println("8. Xem danh sách kho.");
        System.out.println("9. Thêm sản phẩm từ file excel.");
        System.out.println("0. Để thoát");
    }

    public static void addUser(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        String username;
        String password;
        while (true) {
            System.out.print("Nhập tên người dùng: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Tên người dùng không được để trống.");
                continue;
            }
            System.out.print("Nhập mật khẩu: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Mật khẩu không được để trống.");
                continue;
            }
            if(checkUserNameExisted(db, preparedStatement, resultSet, username)) {
                System.out.println("Tên người dùng đã tồn tại.");
                continue;
            }
            break;
        }

        String hashPass;
        hashPass = HashPass.hashPass(password);

        System.out.print("Bạn có muốn gán kho cho user (y/n): ");
        String checkGanWarehouse = scanner.nextLine().trim();

        String warehouse_id = null;

        if(checkGanWarehouse.equals("y")) {
            warehouse_id = chooseWarehouse(db, preparedStatement, resultSet);
        }

        System.out.println("Warehouse id được chọn: " + warehouse_id);

        String sqlQuery = "insert into useradmin (user_id, username, password, role_id, warehouse_id) values (?, ?, ?, ?, ?)";
        String generateUserId = GenerateId.generateStringId();
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(sqlQuery);
            preparedStatement.setString(1, generateUserId);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, hashPass);
            preparedStatement.setString(4, "1");
            preparedStatement.setString(5, warehouse_id);
            int status = preparedStatement.executeUpdate();
            if(status > 0 && warehouse_id != null) {
                if(preparedStatement != null) {
                    preparedStatement.close();
                }
                String queryWarehouse = "update warehouse set user_id = ? where warehouse_id = ?";
                preparedStatement = db.getConnect().prepareStatement(queryWarehouse);
                preparedStatement.setString(1, generateUserId);
                preparedStatement.setString(2, warehouse_id);
                int statusWarehouse = preparedStatement.executeUpdate();
                if(statusWarehouse > 0)
                    System.out.println("Đăng ký thành công.");
                else System.out.println("warehouseId gán cho user nhưng Không thể gán user id cho warehouse id");

            } else if(status > 0) {
                System.out.println("Đăng ký thành công.");
            } else System.out.println("Có lỗi trong quá trình đăng ký user.");
        } catch (SQLException e) {
            System.out.println("Có lỗi trong quá trình đăng ký." + e);
        } finally {
            try {
                if (db != null) db.closeConnect();
                if (preparedStatement != null) preparedStatement.close();
                if (resultSet != null) resultSet.close();
            } catch (SQLException sqlException) {
                System.out.println("Có lỗi trong quá trình đăng ký.");
            }
        }

    }

    public static void editUser(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        boolean hasUser = showUsers(db,preparedStatement,resultSet);
        if(!hasUser) {
            System.out.println("Hiện không có user đẻ thay đổi thông tin");
            return;
        }
        System.out.print("Nhập user id muốn chỉnh sửa: ");
        String userId = scanner.nextLine().trim();

        System.out.println("Bạn muốn chỉnh sửa thông tin gì : ");


    }

    public static void deleteUser(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        boolean hasUser = showUsers(db,preparedStatement,resultSet);
        if(!hasUser) {
            System.out.println("Hiện không có user xóa");
            return;
        }

        System.out.print("Nhập user id muốn xóa: ");
        String userId = scanner.nextLine().trim();

        UserAdmin user = getUserFromId(db, preparedStatement, resultSet, userId);

        if(user == null) {
            System.out.println("user không tồn tại.");
            return;
        }

        if(user.getWarehouseId() == null) {
            implementDeleteUser(db, preparedStatement, resultSet, userId);
            return;
        }


    }

    private static boolean showUsers(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            System.out.println("Danh sách user hiện có");
            String strQuery = "select * from userAdmin where role_id = '1'";
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.isBeforeFirst()) {
                int index = 1;
                while (resultSet.next()) {
                    System.out.println(index + "| Id: " +
                            resultSet.getString("user_id") + "| Tên: " +
                            resultSet.getString("username") + "| kho quản lý : " +
                            resultSet.getString("warehouse_id"));
                    index++;
                }
                return true;
            } else {
                System.out.println("Hiện chưa có user nào để update");
                return false;
            }

        } catch (SQLException sqlErr) {
            System.out.println("In danh sách user có lỗi");
            return false;
        } finally {
            try {
                 if(db != null) db.closeConnect();
                 if(preparedStatement != null) preparedStatement.close();
                 if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("In danh sách user có lỗi");
            }
        }
    }

    private static boolean showUsersNoWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            System.out.println("Danh sách user hiện có");
            String strQuery = "select * from userAdmin where role_id = '1' and warehouse_id is null";
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.isBeforeFirst()) {
                int index = 1;
                while (resultSet.next()) {
                    System.out.println(index + "| Id: " +
                            resultSet.getString("user_id") + "| Tên: " +
                            resultSet.getString("username") + "| kho quản lý : " +
                            resultSet.getString("warehouse_id"));
                    index++;
                }
                return true;
            } else {
                System.out.println("Hiện chưa có user nào để update");
                return false;
            }

        } catch (SQLException sqlErr) {
            System.out.println("In danh sách user có lỗi");
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("In danh sách user có lỗi");
            }
        }
    }

    private static boolean showWarehouses(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            System.out.println("Danh sách user hiện có");
            String strQuery = "select * from warehouse";
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.isBeforeFirst()) {
                int index = 1;
                while (resultSet.next()) {
                    System.out.println(index + "| Id: " +
                            resultSet.getString("warehouse_id") +
                            "| Tên kho: " +
                            resultSet.getString("warehouse_name") +
                            "| địa chỉ kho: " +
                            resultSet.getString("warehouse_address") +
                            "| user quản lý: " +
                            resultSet.getString("user_id")
                    );
                    index++;
                }
                return true;
            } else {
                System.out.println("Hiện chưa có warehouse nào");
                return false;
            }

        } catch (SQLException sqlErr) {
            System.out.println("In danh sách warehouse có lỗi");
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("In danh sách warehouse có lỗi");
            }
        }
    }

    private static boolean showWarehousesNoUser(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            System.out.println("Danh sách warehouse chưa có user hiện có");
            String strQuery = "select * from warehouse where user_id is null";
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.isBeforeFirst()) {
                int index = 1;
                while (resultSet.next()) {
                    System.out.println(index + "| Id: " +
                            resultSet.getString("warehouse_id") +
                            "| Tên kho: " +
                            resultSet.getString("warehouse_name") +
                            "| địa chỉ kho: " +
                            resultSet.getString("warehouse_address") +
                            "| user quản lý: " +
                            resultSet.getString("user_id")
                    );
                    index++;
                }
                return true;
            } else {
                System.out.println("Hiện chưa có warehouse nào chưa có user");
                return false;
            }

        } catch (SQLException sqlErr) {
            System.out.println("In danh sách warehouse có lỗi");
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("In danh sách warehouse có lỗi");
            }
        }
    }

    private static UserAdmin getUserFromId(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String inpUserId) {
        String strQuery = "select * from useradmin where user_id = ? and role_id = 1";
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            preparedStatement.setString(1, inpUserId);
            resultSet = preparedStatement.executeQuery();

            String resUserId = null;
            String userId = "";
            String userName = "";
            String password = "";
            String roleId = "";
            String warehouseId = "";
            UserAdmin user = null;

            if(resultSet.next()) {
                userId = resultSet.getString("user_id");
                userName = resultSet.getString("username");
                password = resultSet.getString("password");
                roleId = resultSet.getString("role_id");
                warehouseId = resultSet.getString("warehouse_id");
                user = new UserAdmin(userId,userName, password, roleId, warehouseId);
            }
            if(db != null) db.closeConnect();
            if(preparedStatement != null) preparedStatement.close();
            if(resultSet != null) resultSet.close();
            return user;
        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi get user trong bảng user" + sqlErr);
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi get user trong bảng user");
            }
        }
        return null;
    }

    private static void implementDeleteUser(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String inpUserId) {
        String strQuery = "delete from useradmin where user_id = ? and role_id = 1";
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            preparedStatement.setString(1, inpUserId);
            int status = preparedStatement.executeUpdate();

            if(status > 0) {
                System.out.println("Xóa user thành công");
            }
        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi xóa userr trong bảng user" + sqlErr);
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi xóa user trong bảng user");
            }
        }
    }

    private static boolean checkUserIdExisted(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String userId) {
        String strQuery = "select username from useradmin where user_id = '" + userId + "' and role_id = 1";
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            resultSet = preparedStatement.executeQuery();

            String resUserId = null;

            if(resultSet.next()) {
                resUserId = resultSet.getString("username");
            } else return false;

            if(resUserId != null) return true;

        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi kiểm tra user trong bảng user" + sqlErr);
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi kiểm tra user trong bảng user");
            }
        }
        return false;
    }

    private static boolean checkUserNameExisted(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String name) {
        String strQuery = "select username from useradmin where username = ? and role_id = 1";
        try {
            System.out.println("strQuery: " + strQuery);
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();

            boolean checkExisted = resultSet.next();

            if(db != null) db.closeConnect();
            if(preparedStatement != null) preparedStatement.close();
            if(resultSet != null) resultSet.close();
            return checkExisted;


        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi kiểm tra user trong bảng user" + sqlErr);
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi kiểm tra user trong bảng user");
            }
        }
    }

    private static boolean checkWarehouseNameExisted(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String name) {
        String strQuery = "select warehouse_name from warehouse where warehouse_name = ?";
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();

            boolean checkExisted = resultSet.next();

            if(db != null) db.closeConnect();
            if(preparedStatement != null) preparedStatement.close();
            if(resultSet != null) resultSet.close();
            return checkExisted;


        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi kiểm tra warehouse name tồn tại trong bảng warehouse" + sqlErr);
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi kiểm tra warehouse name tồn tại trong bảng warehouse");
            }
        }
    }

    private static boolean checkWarehouseIdExisted(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String warehouseId) {
        String strQuery = "select warehouse_name from warehouse where warehouse_id = '" + warehouseId + "'";
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            resultSet = preparedStatement.executeQuery();

            String resUserId = null;

            if(resultSet.next()) {
                resUserId = resultSet.getString("warehouse_name");
            } else return false;

            if(resUserId != null) return true;

        } catch (SQLException sqlErr) {
            System.out.println("Lỗi kiểm tra warehouse trong bảng warehouse");
            return false;
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi kiểm tra warehouse trong bảng warehouse");
            }
        }
        return false;
    }

    public static void addWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        String warehouseName;
        String warehouseAddress;
        while (true) {
            System.out.print("Nhập tên kho: ");
            warehouseName = scanner.nextLine().trim();
            if (warehouseName.isEmpty()) {
                System.out.println("Tên kho không được để trống.");
                return;
            }
            System.out.print("Địa chỉ kho: ");
            warehouseAddress = scanner.nextLine().trim();
            if(checkWarehouseNameExisted(db, preparedStatement, resultSet, warehouseName)) {
                System.out.println("Warehouse đã tồn tại.");
                continue;
            }
            break;
        }

        System.out.print("Bạn có muốn gán user cho kho không (y/n): ");
        String checkGanUser = scanner.nextLine().trim();

        String userId = null;

        if(checkGanUser.equals("y")) {
            userId = chooseUser(db, preparedStatement, resultSet);
        }

        if(userId == null && checkGanUser.equals("y")) {
            System.out.println("Hiện chưa có user nào.");
        }

        String sqlQuery = "insert into warehouse (warehouse_id, warehouse_name, warehouse_address, user_id) values (?, ?, ?, ?)";
        String generateWarehouseId = GenerateId.generateStringId();
        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(sqlQuery);
            preparedStatement.setString(1, generateWarehouseId);
            preparedStatement.setString(2, warehouseName);
            preparedStatement.setString(3, warehouseAddress);
            preparedStatement.setString(4, userId);
            int status = preparedStatement.executeUpdate();
            if(status > 0 && userId != null) {
                if(preparedStatement != null) {
                    preparedStatement.close();
                }
                String queryWarehouse = "update useradmin set warehouse_id = ? where user_id = ?";
                preparedStatement = db.getConnect().prepareStatement(queryWarehouse);
                preparedStatement.setString(1, generateWarehouseId);
                preparedStatement.setString(2, userId);
                int statusUser = preparedStatement.executeUpdate();
                if(statusUser > 0)
                    System.out.println("Tạo kho thành công.");
                else System.out.println("warehouse được gán cho 1 user nhưng ở user không thể gán warehouse");
            } else if(status > 0) System.out.println("Tạo kho thành công.");
            else System.out.println("Có lỗi trong quá trình tạo kho.");
        } catch (SQLException e) {
            System.out.println("Có lỗi trong quá trình tạo kho." + e);
        } finally {
            try {
                if (db != null) db.closeConnect();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException sqlException) {
                System.out.println("Có lỗi trong quá trình tạo kho.");
            }
        }

    }

    public static void editWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        boolean hasUser = showWarehouses(db,preparedStatement,resultSet);
        if(!hasUser) {
            System.out.println("Hiện không có warehouse đẻ thay đổi thông tin");
            return;
        }
        System.out.print("Nhập warehouse id muốn chỉnh sửa: ");
        String userId = scanner.nextLine().trim();

        System.out.println("Bạn muốn chỉnh sửa thông tin gì : ");


    }

    public static void deleteWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        boolean hasUser = showWarehouses(db,preparedStatement,resultSet);
        if(!hasUser) {
            System.out.println("Hiện không có warehouse nào");
            return;
        }
        System.out.print("Nhập warehouse id muốn xóa: ");
        String userId = scanner.nextLine().trim();

        System.out.println("Bạn muốn chỉnh sửa thông tin gì : ");


    }

    private static String chooseUser(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        boolean hasUser = showUsersNoWarehouse(db, preparedStatement, resultSet);
        if(!hasUser) {
            return null;
        }
        System.out.print("Chọn user gán vào kho (nhập user id) : ");
        String userId = scanner.nextLine().trim();

        boolean isUserExisted = checkUserIdExisted(db, preparedStatement, resultSet, userId);

        if(!isUserExisted) return null;

        return userId;
    }

    private static String chooseWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        boolean hasUser = showWarehousesNoUser(db, preparedStatement, resultSet);
        if(!hasUser) {
            return null;
        }
        System.out.print("Chọn warehouse gán cho user (nhập warehouse id) : ");
        String userId = scanner.nextLine().trim();

        boolean isWarehouseExisted = checkWarehouseIdExisted(db, preparedStatement, resultSet, userId);

        if(!isWarehouseExisted) return null;

        return userId;
    }

    public static void nhapSpVaoKho(String warehouseId,DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        ArrayList<Product> listProduct = new ArrayList<>();
        listProduct = FileHandle.readExcel("DanhSachSP.xlsx");
    }

    public static void loginMain(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        System.out.println("Đăng nhập tài khoản.");
        System.out.print("Tên dăng nhập: ");
        String tenTK = scanner.nextLine().trim();
        System.out.print("mật khẩu: ");
        String mk = scanner.nextLine().trim();

        checkLoginUser(tenTK, mk, db, preparedStatement, resultSet);
    }

    private static void checkLoginUser(String tk, String mk,DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
            System.out.println("thông tin : " + tk + " - " + mk);
        try {
            String hashPass = HashPass.hashPass(mk);
            String strQuery = "select * from useradmin where username = ?";
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQuery);
            preparedStatement.setString(1, tk);
            resultSet = preparedStatement.executeQuery();
            String userId = "";
            String userName = "";
            String password = "";
            String roleId = "";
            String warehouseId = "";

            if(resultSet.next()) {
                userId = resultSet.getString("user_id");
                userName = resultSet.getString("username");
                password = resultSet.getString("password");
                roleId = resultSet.getString("role_id");
                warehouseId = resultSet.getString("warehouse_id");
            } else {
                if (db != null) db.closeConnect();
                if (preparedStatement != null) preparedStatement.close();
                if (resultSet != null) resultSet.close();
                System.out.println("Không tìm thấy user.");
                return;
            }

            if(!hashPass.equals(password)) {
                if (db != null) db.closeConnect();
                if (preparedStatement != null) preparedStatement.close();
                if (resultSet != null) resultSet.close();
                System.out.println("mật khẩu hoặc tk chưa đúng.");
                return;
            }

            System.out.println("Đăng nhập thành công.");

            currentUser = new UserAdmin(userId, userName, null, roleId, warehouseId);


        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
        } finally {
            try {
                if (db != null) db.closeConnect();
                if (preparedStatement != null) preparedStatement.close();
                if (resultSet != null) resultSet.close();
            } catch (SQLException sqlException) {
                System.out.println("Có lỗi trong quá trình đăng nhập.");
            }
        }
    }

    private static boolean checkWarehouseIsEmpty(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String warehouseId) {
        return false;
    }

    private static void addProductToWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String warehouseId) {
        if(warehouseId == null) {
            System.out.println("User chưa có kho. Yêu cầu Admin gán kho.");
            return;
        }


        ArrayList<Product> listProduct = FileHandle.readExcel("DanhSachSP.xlsx");
        String strQueryProduct = "insert into product (product_id, product_name, quantity, warehouse_id) values (?, ?, ?, ?)";
        String strQueryProductAttr = "insert into product_attrs (product_attr_id, description, price, product_id) values (?, ?, ?, ?)";
        PreparedStatement preparedStatementAttr = null;

        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQueryProduct);
            preparedStatementAttr = db.getConnect().prepareStatement(strQueryProductAttr);

            System.out.println("length: " + listProduct.size());

            db.getConnect().setAutoCommit(false);

            for(Product prod: listProduct) {
                String prodId = "prod" + GenerateId.generateStringId();
                String prodAttrId = "prodAttr" + GenerateId.generateStringId();
                System.out.println("product: " + prod.getSanpham() + " = " + prod.getSoLuong() + " = " + warehouseId);


                preparedStatement.setString(1,prodId);
                preparedStatement.setString(2, prod.getSanpham());
                preparedStatement.setString(3, prod.getSoLuong());
                preparedStatement.setString(4, warehouseId);
                preparedStatement.executeUpdate();

                preparedStatementAttr.setString(1, prodAttrId);
                preparedStatementAttr.setString(2, prod.getMoTa());
                preparedStatementAttr.setString(3, prod.getGiaSP());
                preparedStatementAttr.setString(4, prodId);
                preparedStatementAttr.executeUpdate();
            }

            db.getConnect().commit();

            System.out.println("Sản phẩm được thêm thành công");

        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi không thể thêm product vào warehouse");
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
                if(preparedStatementAttr != null) preparedStatementAttr.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi không thể thêm product vào warehouse");
            }
        }

//        boolean checkKhoIsEmpty = checkWarehouseIsEmpty(db, preparedStatement, resultSet, warehouseId);

    }

    public static void showProductInWarehouse(DB db, PreparedStatement preparedStatement, ResultSet resultSet, String warehouseId) {
        if(warehouseId == null) {
            System.out.println("User chưa có kho. Yêu cầu Admin gán kho.");
            return;
        }
        String strQueryProduct = "select * from product join product_attrs on product.product_id = product_attrs.product_id " +
                "where product.warehouse_id = ?";

        try {
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(strQueryProduct);


            preparedStatement.setString(1, warehouseId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println("tên sp: " + resultSet.getString("product_name") +
                        " == số lượng == " + resultSet.getString("quantity") +
                        " == mô tả == " + resultSet.getNString("description") +
                        " == giá == " + resultSet.getString("price")
                );
            }

        } catch (SQLException sqlErr) {
            sqlErr.printStackTrace();
            System.out.println("Lỗi không thể thêm product vào warehouse");
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException sqlErrr) {
                System.out.println("Lỗi không thể thêm product vào warehouse");
            }
        }
    }
}


/*
* private static void showRoles(DB db, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            System.out.println("Danh sách vai trò");
            String sqlQuery = "select * from role";
            db.openConnect();
            preparedStatement = db.getConnect().prepareStatement(sqlQuery);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.isBeforeFirst()) {
                int index = 1;
                while (resultSet.next()) {
                    System.out.println(index + " role Id:  " +  resultSet.getString("role_id") + " role name " + resultSet.getString("role_name"));
                    index++;
                }
            } else System.out.println("Không có dữ liệu trong hệ thống.");

        } catch (SQLException err) {
            System.out.println("Có lỗi trong quá trình lấy danh sách vai trò.");
        } finally {
            try {
                if(db != null) db.closeConnect();
                if(preparedStatement != null) preparedStatement.close();
                if(resultSet != null) resultSet.close();
            } catch (SQLException errr) {
                System.out.println("Có lỗi trong quá trình lấy danh sách vai trò.");
            }
        }
    }
*
* */