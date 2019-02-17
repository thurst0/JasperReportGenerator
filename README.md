# JasperReportGenerator

Download the contents of the "dist" folder to your computer to use this.  
Runnable JAR file that takes a few parameters and outputs a PDF.  It will also return a string of performance statistics on success, or a erorr, and callstack on fail.

1.) Jasper JRXML or JASPER file path.  If it is passed the JRXML it will re-compile the JASPER file.  Sometimes if you put in the JASPER file that was compiled by JasperReports Studio, it won't work because of versioning.  It should work if you let the JAR compile your JRXML.
2.) Output file path.  This is where you want the output of the report to go.  
3.) JSON parameters.  These are the parameters you want passed to your report in JSON format.  
4.) Datasource.  Currently only supports MSSQL, or no datasource.  TODO for mysql, postgres, csv, json.  
5.) Datasource connection string (optional)  
  
Example Call using windows  
  
java -jar JasperReportGenerator.jar "C:\\test.jrxml" "C:\\test.pdf" "{'AgentID':'thurst','Text':'Chuck Testa'}" "MSSQL" "jdbc:sqlserver://localhost;databaseName=LightTaskApp;user=LTAdmin;password=*****"
  
