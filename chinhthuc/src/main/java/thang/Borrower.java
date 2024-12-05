package thang;

public class Borrower {

  private String name;
  private int idBorrower;
  private int age;
  private String address;
  private String sex;
  private String phone;
  private String email;

  public void setAddress(String address) {
    this.address = address;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  public String getAddress() {
    return address;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public int getIdBorrower() {
    return idBorrower;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getSex() {
    return sex;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public Borrower() {

  }

  public Borrower(String name, String sex, int age, String email, String phone, String address) {
    this.address = address;
    this.name = name;
    this.age = age;
    this.email = email;
    this.phone = phone;
    this.sex = sex;
  }

  public Borrower(int idBorrower, String name, String sex, int age, String email, String phone,
      String address) {
    this.idBorrower = idBorrower;
    this.address = address;
    this.name = name;
    this.age = age;
    this.email = email;
    this.phone = phone;
    this.sex = sex;
  }

  public Borrower(int idBorrower, String name, String email) {
    this.idBorrower = idBorrower;
    this.name = name;
    this.email = email;
  }


}

