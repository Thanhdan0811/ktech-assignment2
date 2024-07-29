package models;

public class UserAdmin {
    private String userId;
    private String userName;
    private String password;
    private String roleId;
    private String warehouseId;

    public UserAdmin(String userId, String name, String pass, String roleId, String warehouseId) {
        this.userId = userId;
        this.userName = name;
        this.password = pass;
        this.roleId = roleId;
        this.warehouseId = warehouseId;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public  void showInfo() {
        System.out.println(
                "user Id: " + this.userId +
                " | tên user: "  + this.userName +
                " | kho Id: " + this.warehouseId
        );
    }
}
