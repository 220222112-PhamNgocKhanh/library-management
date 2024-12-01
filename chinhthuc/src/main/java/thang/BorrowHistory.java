package thang;

import java.sql.Date;

public class BorrowHistory {

    private int idDocument;
    private int idBorrower;
    private Date borrowDate;
    private Date returnDate;
    private String status;
    private String title;

    public BorrowHistory(int idDocument, int idBorrower, Date borrowDate, Date returnDate, String status) {
        this.idDocument = idDocument;
        this.idBorrower = idBorrower;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }
    public BorrowHistory(int idDocument,String title, Date borrowDate, Date returnDate, String status) {
        this.idDocument = idDocument;
        this.title = title;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }


    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getTitle() {
        return title;
    }


    public int getIdDocument() {
        return idDocument;
    }

    public int getIdBorrower() {
        return idBorrower;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }
}
