package src;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect() {

        try {

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://kodama.proxy.rlwy.net:42335/railway",
                "root",
                "nDoJoIDLcFJCynurqsZjLAkhbAgkOmJa"
            );

            System.out.println("Database Connected Successfully");

            return con;

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }
}