import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ReadWriteExcel {

	private static ArrayList<String> contentFOutput;
	private static ArrayList<String> contentFresult;
	
	public ReadWriteExcel()
	{
		contentFOutput = new ArrayList<String>();
        contentFresult = new ArrayList<String>();
	}
	
	//Ghi du lieu thu duoc xuong file
	public static void WriteFile(String path, String content) throws IOException
    {
    	
    	File file = new File(path+".txt");
    	if(!file.exists())
    	{
    		file.createNewFile();
    		
    	}
    	FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
    	BufferedWriter bw = new BufferedWriter(fw);
    	bw.write(content);
    	bw.newLine();
      	bw.close();
    }
	
	//xu li file chuyen doi du lieu audio sang text, ghi xuong file
	public void ProcessFile(String pathFolder, Configuration configuration) throws IOException
    {
    	File folder = new File(pathFolder);
    	int size = folder.listFiles().length;
    	
    	for(int i = 0 ; i < size;i++)
    	{
    		//double start = System.currentTimeMillis();
    		File file = folder.listFiles()[i];
    		StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            InputStream stream = new FileInputStream(file);
            
            recognizer.startRecognition(stream);
            SpeechResult result;
            while ((result = recognizer.getResult()) != null) {
                WriteFile("./output/"+file.getName().substring(0,file.getName().length()-4), result.getHypothesis());
               
            }
            recognizer.stopRecognition();
            //ghi thoi gian chay
            //double end = System.currentTimeMillis();
            //WriteFile("TimeRun", String.valueOf((end - start)/1000));
    	}
    }
	
	//Ghi ket qua thu duoc vao file excel
	public void WriteResultExcel(String pathFResult, String pathFOutput, String pathExcel) throws BiffException, IOException, RowsExceededException, WriteException
    {
    	int size = 0;
    	//ghi vao file exel
    	Workbook wb = Workbook.getWorkbook(new File(pathExcel));
    	WritableWorkbook writewb = Workbook.createWorkbook(new File(pathExcel), wb);
    	WritableSheet sheet = writewb.getSheet(0);
    	File folderResult = new File(pathFResult);
    	File folderOutput = new File(pathFOutput);
    	//File TimeRun = new File("TimeRun.txt");
    	//size = folderResult.listFiles().length;
    	size = folderOutput.listFiles().length;
    	//Scanner scan = new Scanner(TimeRun);
    	for(int i = 0; i < size;i++)
    	{
    		try
    		{
	    		//if(i < folderOutput.listFiles().length)
	    		{
	    			File result = folderResult.listFiles()[0];
		    		File output = folderOutput.listFiles()[i];
		    		
		    		//double time = scan.nextDouble();
		    		//if(output.getName().equals(result.getName()))
		    		{
			    		sheet.addCell(new Number(0,i+2, i +1));
			    		sheet.addCell(new Label(1, i+2, output.getName().substring(0,output.getName().length()-4)));
			    		sheet.addCell(new Number(4,i+2, PercentSame(output, result)));
			    		sheet.addCell(new Label(2,i+ 2, contentFOutput.toString()));
			    		sheet.addCell(new Label(3,i+ 2, contentFresult.toString()));
			    		//sheet.addCell(new Number(8,i+2, PercentSame(output, result)));
			    		//sheet.addCell(new Number(5,i+2, time));
		    		}
	    		}
    		}catch(Exception ex){}
    		contentFOutput.clear();
    		contentFresult.clear();
    	}
    	
    	writewb.write();
    	writewb.close();	
    }
	
	//Ghi thong tin nguoi thu am
	public void WritePeopleExcel(String pathFPeople, String pathExcel)
	{
		try
		{
			Workbook wb = Workbook.getWorkbook(new File(pathExcel));
	    	WritableWorkbook writewb = Workbook.createWorkbook(new File(pathExcel), wb);
	    	WritableSheet sheet = writewb.getSheet(0);
	    	File folderFile = new File(pathFPeople);
	    	int size = folderFile.listFiles().length;
	    	for(int i = 0; i < size; i++)
	    	{
	    		String []str = folderFile.listFiles()[i].getName().split("_");
	    		sheet.addCell(new Label(0, i+2, str[0]));
	    		sheet.addCell(new Label(1, i+2, str[1]));
	    		sheet.addCell(new Label(2, i+2, str[2]));
	    		sheet.addCell(new Label(3, i+2, str[3]));
	    		sheet.addCell(new Label(4, i+2, str[4]));
	    		
	    	}
	    	writewb.write();
	    	writewb.close();	
		}catch(Exception ex)
		{
			
		}
		
	}
	//so sanh ket qua thu duoc voi ket qua chinh xac
    public static float PercentSame(File file1, File file2) throws FileNotFoundException
	{
    	int size = 0;
		float kq = 0;
		int sizeChar = 0;
		FileReader frOutput = new FileReader(file1);
		BufferedReader read1 = new BufferedReader(frOutput);
		FileReader frResult = new FileReader(file2);
		BufferedReader read2 = new BufferedReader(frResult);
				
		try {
			String strResult = read2.readLine();
			while(strResult != null)
			{
				String strOutput = read1.readLine();
				if(strOutput != null)
				{
					contentFOutput.add(strOutput);
					//strOutput = read1.readLine();
				}
				contentFresult.add(strResult);
				strResult = read2.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		///////////////////////////////////////////////////
		int sizeResult = contentFresult.size();
		int dem = 0;
		String result = "";
		for(int i = 0; i < sizeResult;i++)
			result += " " +contentFresult.get(i);
		
		int sizeOutput = contentFOutput.size();
		String output ="";
		for(int i = 0; i < sizeOutput;i++)
			output += " " +contentFOutput.get(i);
		
		String []strR = result.split(" ");
		String []strO = output.split(" ");
		for(int i = 0 ; i < strR.length;i++)
		{
			if(i < strO.length && strR[i].equals(strO[i]))
			{
				kq++;
			}
			sizeChar++;
		}
		dem++;
		//////////////////////////////////////////////////////
		//so sanh tung ky tu tung dong
		/*while(dem < size)
		{
			String str1 = contentFresult.get(dem);
			String str2 = null;
			if(dem < contentFOutput.size())
				str2 = contentFOutput.get(dem);
			else
				break;
			for(int i = 0 ; i < str1.length();i++)
			{
				if(i < str2.length() && str1.charAt(i) == str2.charAt(i))
				{
					kq++;
				}
				sizeChar++;
			}
			dem++;
			
		}*/
		return kq = kq/sizeChar*100;
	}
}
