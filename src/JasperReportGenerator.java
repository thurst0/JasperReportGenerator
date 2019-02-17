
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.io.File;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterName;

import org.json.JSONException;
import org.json.JSONObject;

import com.lowagie.text.List;

import groovyjarjarantlr.StringUtils;
import net.sf.jasperreports.components.barcode4j.*;
import net.sf.jasperreports.components.barbecue.*;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
//import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
//import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
//import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
//import net.sf.jasperreports.functions.*;

//import java.sql.*;

public class JasperReportGenerator {
   @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception, JSONException, SQLException, IOException {
		String result = generateReport(args);
		System.out.println(result);
	} 

	public static JasperPrint fillReportFromSQLDataSource(JasperReport jasperReport, String connstr, Map<String, Object> parameters)
	throws ClassNotFoundException, SQLException, JRException{
		//get SQL connection
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection conn = DriverManager.getConnection(connstr);
		return JasperFillManager.fillReport(jasperReport, parameters, conn); 
	}

	public static JasperPrint fillReportFromEmptyDataSource(JasperReport jasperReport, Map<String, Object> parameters) 
		throws JRException
	{
		return JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1)); 		
	}  
 
	public static String generateReport(String[] args) throws Exception, JSONException, SQLException, IOException{
		if(args.length < 4){
		 	return "Missing required parms. ";
		}
		  
		String rptSrcFile = args[0];
		String rptOutputFile = args[1];
		String rptInputParms = args[2];
		String datasource = args[3];
		String connstr = args[4];

		// String rptInputParms = "{'AgentID':'THURST', 'Text':'Chuck Testa'}";
		// String rptSrcFile = "c:\\source\\jasperreportgenerator\\rpt\\Test.jrxml";
		// String rptOutputFile = "c:\\source\\jasperreportgenerator\\rpt\\output\\Test.pdf";
		// String connstr = "jdbc:sqlserver://localhost;databaseName=LightTaskApp;user=LTAdmin;password=LT123;";
		// String datasource = "mssql";

		String perfLog = "";
		long tmpNanoSeconds = 0;

		try{
			tmpNanoSeconds = System.nanoTime();
			if(rptSrcFile.endsWith(".jrxml")){ // if we pass in jrxml assume we want to compile
				JasperCompileManager.compileReportToFile(rptSrcFile, rptSrcFile.replace(".jrxml", ".jasper")); // output
				rptSrcFile = rptSrcFile.replace(".jrxml", ".jasper");
			}
			// load jasper file
			JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(rptSrcFile);
			perfLog = "Compilation time : " + ((System.nanoTime() - tmpNanoSeconds)/ 1000000000.0) + " s. ";
			
			Map<String, Object> parameters = new HashMap<String, Object>();

			// parse JSON parms
			if(rptInputParms != null && rptInputParms != ""){
			
				JSONObject inputJSONParms = new JSONObject(rptInputParms); // convert input parms to JSON obj
				JRParameter[] rptJSONParms = jasperReport.getParameters(); // get parms from jasper report
				
				String jasperParm;
				Object parmValue;

				// for jasper parms find matching passed in parm
				for(JRParameter param : rptJSONParms) {
					if(!param.isSystemDefined() && param.isForPrompting()){
						jasperParm = param.getName();
						if (inputJSONParms.has(jasperParm)){ // user defined parm and passed in to args
							parmValue = inputJSONParms.get(jasperParm);
							parameters.put(jasperParm, parmValue); // todo handle data types
						}
					}
				}
			
			}

			tmpNanoSeconds = System.nanoTime();

			JasperPrint print;

			switch(datasource.toUpperCase()){
				case "MSSQL":
					print = fillReportFromSQLDataSource(jasperReport, connstr, parameters);
					break;
				default:
					print = fillReportFromEmptyDataSource(jasperReport, parameters);
					break;
				// todo postgres, mysql, csv, json, etc.
			}

			perfLog += "Fill time : " + ((System.nanoTime() - tmpNanoSeconds)/ 1000000000.0) + " s. ";
			
			tmpNanoSeconds = System.nanoTime();

			// export to pdf
			JRPdfExporter exporter = new JRPdfExporter();
			ExporterInput exporterInput = new SimpleExporterInput(print);
			exporter.setExporterInput(exporterInput);
			OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(rptOutputFile);
			exporter.setExporterOutput(exporterOutput);
			SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
			exporter.setConfiguration(configuration);
			exporter.exportReport(); 

			perfLog += "Export time : " + ((System.nanoTime() - tmpNanoSeconds)/ 1000000000.0) + " s. ";
			
		// }catch(JRException e){
		// 	return e.getMessage();
		}catch(JSONException e){
			return e.getMessage();
		}catch(SQLException e){
			return e.getMessage();
	   	//}catch(IOException e){
		//	return e.getMessage();
   		}catch(Exception e){
			e.printStackTrace();
			return e.getMessage() + " " + Arrays.toString(e.getStackTrace());

		}
		return "Success Stats : " + perfLog;
	}
}

