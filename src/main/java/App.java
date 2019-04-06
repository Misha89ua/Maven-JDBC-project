import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Scanner;

public class App {

    static Connection connection;

    public static void main(String[] args) throws SQLException {

        //DATABASE SETUP
        String url = "jdbc:mysql://localhost:3306/demoDB";       //url

        Properties properties = new Properties();
        properties.setProperty("user", "root");                  //login
        properties.setProperty("password", "1111");              //password
        properties.setProperty("useSSL", "false");
        properties.setProperty("autoReconnect", "true");

        connection = DriverManager.getConnection(url,properties);
        System.out.println("Connection" + " - " + !connection.isClosed());

        createTables();
        fillingTables();
        menu();

        connection.close();
    }

    public static void menu() throws SQLException {
        System.out.println("\n\nОберіть запит:");
        System.out.println("1 - Покажет превысил ли доход сотрудника за год отметку в 100.000");
        System.out.println("2 - Выведет сотрудников старше 50 лет у которых зарплата за какой либо месяц прошлого года была меньше 5.000");
        System.out.println("0 - Вихід");
        Scanner scan = new Scanner(System.in);
        while (true) {
            int number = scan.nextInt();
            if (number == 1){
                System.out.println("Уведіть ID працівника");
                int id = scan.nextInt();
                request1(id);
            }else
            if (number == 2){
                request2();
            }else
            if (number == 0){
                break;
            }else
                System.out.println("невірне введення");
            System.out.println("\n\nОберіть запит:");
            System.out.println("1 - Покажет превысил ли доход сотрудника за год отметку в 100.000");
            System.out.println("2 - Выведет сотрудников старше 50 лет у которых зарплата за какой либо месяц прошлого года была меньше 5.000");
            System.out.println("0 - Вихід");
        }
    }

    public static void createTables() throws SQLException {

        Statement stm = null;

        stm = connection.createStatement();

        stm.execute("drop table if exists salary;");

        stm.execute("drop table if exists worker;");

        stm.execute("create table worker(" +
                "id int primary key auto_increment," +
                "first_name varchar(45)," +
                "last_name varchar(45)," +
                "age int );");

        stm.execute("create table salary(" +
                "id int primary key auto_increment," +
                "mon varchar (15)," +
                "sal decimal(8,2)," +
                "worker_id int" +
                ");");

        stm.execute("alter table salary add foreign key(worker_id) references worker(id);");
    }

    public static void fillingTables() throws SQLException{

        Statement stm = null;

        stm = connection.createStatement();

        stm.execute("insert into worker(first_name, last_name, age)" +
                "values" +
                "(\"Ivan\", \"Petrov\", 33)," +
                "(\"Petro\", \"Boyko\", 59)," +
                "(\"Mykhailo\", \"Boyko\", 25)," +
                "(\"Sofi\", \"Potsiluyko\", 66)," +
                "(\"Oksana\", \"Faruna\", 41);");

        for(int year = 2000; year < 2019; year++){
            for (int month = 1; month <= 12; month++){
                for (int worker_id = 1; worker_id <= 5; worker_id++ ){
                    LocalDate date = Date.valueOf(year + "-" + month + "-01").toLocalDate();
                    stm.execute("insert into salary(mon, sal, worker_id)" +
                            "values" +
                            "(\"" + date.getYear() + "-" + date.getMonthValue() + "\", " + (4000 + Math.random() * 7000) + ", " + worker_id + ");");
                }
            }
        }
    }

    public static void request1(int workerId) throws SQLException {

        Statement stm = null;

        for (int year = 2000; year < 2019; year++) {
            stm = connection.prepareStatement("select s.sal FROM salary s where s.mon between '" + year + "-1' and '" + year + "-9' and s.worker_id = " + workerId);
            ResultSet rs = ((PreparedStatement) stm).executeQuery();
            long yearSalary = 0;
            while (rs.next()){
                yearSalary = yearSalary +rs.getLong("sal");
            }
            if (yearSalary > 100000){
                System.out.println("У даного працівника річна зарплата перевищила 100000 у " + year + "році.");
            }
        }
    }

    public static void request2() throws SQLException {

        Statement stm = null;
        stm = connection.prepareStatement("SELECT * FROM salary s join worker w on w.id = s.worker_id where w.age > 50 and s.mon between '2018-1' and '2018-9' and s.sal < 5000;");
        ResultSet rs = (((PreparedStatement) stm).executeQuery());
        while (rs.next()){
            System.out.println("працівник " + rs.getString("first_name") + " " + rs.getString("last_name")
                    + " " + rs.getString("age") + "років отримав " + rs.getString("sal") + "........." + rs.getString("mon"));
        }

    }

}
