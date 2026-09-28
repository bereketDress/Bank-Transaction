package com.bankofcli.exception;
/**
SQLException e = new SQLException("Connection refused");

System.out.println(e.getMessage());
System.out.println(e);
e.printStackTrace();

e.getMessage()
→ prints only the error message
// Connection refused

System.out.println(e)
→ prints exception type + message
// java.sql.SQLException: Connection refused

e.printStackTrace()
→ prints type + message + where the error happened
// java.sql.SQLException: Connection refused
//     at AccountRepository.save(AccountRepository.java:42)
//     at BankService.createAccount(BankService.java:18)
//     at Main.main(Main.java:10)
*/
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }
    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}







// message = custom message for this BankException like  new BankException("failed)
// cause   = original exception object that caused this BankException like arithmetic exception


