package nl.hva;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet(name = "/login.jsp", urlPatterns = {"/login.jsp"})


public class ServletSample extends HttpServlet {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String targeturl = "http://fys.securidoc.nl:11111/Ticket";
    public static boolean ticketding = false;
    public static int ticketCorrect;
    public static String ipklant;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // read form fields, opgegeven informatie uit FORM uit INDEX.jsp

        //krijg ip van klant
        ipklant = request.getRemoteAddr();



        String ticketnummerform = request.getParameter("ticketnummerform");
        String voornaam = request.getParameter("voornaam");
        String achternaam = request.getParameter("achternaam");

        System.out.println("ticketnummer: " + ticketnummerform);


        //This is where the magic happens

        //Verstuur POST request naar api en stur gegevens mee
        try {
            ServletSample.sendJson("{\"function\":\"Check\" , \"teamId\":\"IC106-3\" , \"teamKey\":\"28b783164c4fef17699b297c7060517b\" , \"requestId\":\"1\" , \"firstName\":\"" + voornaam + "\" , \"lastName\":\"" + achternaam + "\" , \"ticketNumber\":\"" + ticketnummerform + "\"}");


            //Is stuk, dan error
        } catch (IOException e) {
            e.printStackTrace();
        }


        PrintWriter out = response.getWriter();

        //Dit checkt niet de eerste keer, maar reageert afhankelijk van de eerste check
        if (ticketCorrect == 1) {

            //voer een command uit op de pi
            Runtime.getRuntime().exec("sudo iptables -I PREROUTING -t nat -s "+ ipklant + " -j accept");
            Runtime.getRuntime().exec("sudo iptables -I FORWARD -s "+ ipklant + " -j accept");





            response.sendRedirect("https://www.corendon.nl/");
            System.out.println("DIT IS true");
            ticketding = true;

        } else {
            response.sendRedirect("http://10.3.141.1:8095/web_warprojectfys/test.jsp");
            System.out.println("DIT IS false");
            response.setContentType("text/html");
            out.println("<script type=\"text/javascript\">");
            out.println("alert('deadbeef');");
            out.println("</script>");

            ticketding = false;
        }

    }

    public static void sendJson(String json) throws IOException {

        //method call for generating json

        URL myurl = new URL(targeturl);
        HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);

        //Ziet er intens uit maar niet critical
        con.setRequestProperty("Content-Type", "application/json;");
        con.setRequestProperty("Accept", "application/json,text/plain");
        con.setRequestProperty("Method", "POST");
        OutputStream os = con.getOutputStream();
        os.write(json.toString().getBytes("UTF-8"));
        os.close();

        //Zet de basis op en zet de response naar een string
        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            //Zet string naar json, belangrijk
            String jsonResponseString = sb.toString();


            //-----------PARSEN IS SUPER BELANGRIJK VANAF BLIJVEN-----------

            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(jsonResponseString);
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            int ticketStatus = (jsonObject.get("result").getAsInt());

            System.out.println(ticketStatus);

            //-----------Einde parsen-----------


            //Als ticketstatus 0 is, dan is hij valid, doe dan iptables magie
            if (ticketStatus == 0) {
                ticketding = true;
                ticketCorrect = 1;
                System.out.println("Helemaal mooi man");
                System.out.println(ticketding);

                Runtime.getRuntime().exec("sudo touch testing.txt");
                Runtime.getRuntime().exec("sudo iptables -I PREROUTING -t nat -s "+ ipklant + " -j ACCEPT");
                Runtime.getRuntime().exec("sudo iptables -I FORWARD -s "+ ipklant + " -j ACCEPT");
                Runtime.getRuntime().exec("sudo touch werktditwel.txt");


                //voer een command uit op de pi

            } else {
                ticketding = false;
                ticketCorrect = 0;
                System.out.println("je kan niks");
                System.out.println(ticketding);
            }

            //Er is iets goed mis
        } else {
            System.out.println(con.getResponseCode());
            System.out.println(con.getResponseMessage());
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //net zo leeg als mijn ziel

    }

}