package cn.zero.spider.pojo;

public  class ResBean {
    /**
     * isValid : 1
     * phone : 13888888888
     * nickName :
     * openId :
     * userIconUrl :
     * userIconUrl2 :
     * userIconUrl3 :
     * email :
     * operator :
     *
     */

    private int isValid;
    private String phone;
    private String nickName;
    private String openId;
    private String userIconUrl;
    private String userIconUrl2;
    private String userIconUrl3;
    private String email;
    private String operator;

    public String getMobileToken() {
        return mobileToken;
    }

    public void setMobileToken(String mobileToken) {
        this.mobileToken = mobileToken;
    }

    private String mobileToken;


    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUserIconUrl() {
        return userIconUrl;
    }

    public void setUserIconUrl(String userIconUrl) {
        this.userIconUrl = userIconUrl;
    }

    public String getUserIconUrl2() {
        return userIconUrl2;
    }

    public void setUserIconUrl2(String userIconUrl2) {
        this.userIconUrl2 = userIconUrl2;
    }

    public String getUserIconUrl3() {
        return userIconUrl3;
    }

    public void setUserIconUrl3(String userIconUrl3) {
        this.userIconUrl3 = userIconUrl3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
