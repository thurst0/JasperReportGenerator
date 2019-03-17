# JasperReportGenerator

Download the contents of the "dist" folder to your computer to use this.  
Runnable JAR file that takes a few parameters and outputs a PDF.  
It will return a JSON string on success or error.
On error JSON string will contain "msg".  Where "msg" is the error and call stack.
On success JSON string will contain "msg" and "data" properties.  Where "msg" is the performance statitstics, and "data" is the PDF as base64 output stream.

1.) Jasper JRXML or JASPER file path.  If it is passed the JRXML it will re-compile the JASPER file.  Sometimes if you put in the JASPER file that was compiled by JasperReports Studio, it won't work because of versioning.  It should work if you let the JAR compile your JRXML. 
2.) JSON parameters.  These are the parameters you want passed to your report in JSON format.    
3.) Datasource.  Currently only supports MSSQL, or no datasource.  TODO for mysql, postgres, csv, json.  
4.) Datasource connection string (optional)    
5.) Output file path (optional).  This is where you want the output of the report to go.  Alternatively you can grab the base 64 output stream from the output JSON    

Example Call using windows or linux

java -jar JasperReportGenerator.jar C:\source\jasperreportgenerator\rpt\test.jrxml "{'AgentID':'thurst','Text':'Chuck Testa'}" MSSQL jdbc:sqlserver://localhost;databaseName=LightTaskApp;user=LTAdmin;password=test123 C:\source\test2.pdf

If you are using node, be careful to escape necessary quotes and whacks properly.

