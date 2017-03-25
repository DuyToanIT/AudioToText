
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.speakerid.Segment;
import edu.cmu.sphinx.speakerid.SpeakerCluster;
import edu.cmu.sphinx.util.TimeFrame;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class SpeakerIdentification {

	private static ArrayList<String> contentFOutput;
	private static ArrayList<String> contentFresult;
    /**
     * Returns string version of the given time in milliseconds
     * 
     * @param milliseconds time in milliseconds
     * @return time in format mm:ss
     */
    public static String time(int milliseconds) {
        return (milliseconds / 60000) + ":"
                + (Math.round((double) (milliseconds % 60000) / 1000));
    }

    /**
     * 
     * @param speakers
     *            An array of clusters for which it is needed to be printed the
     *            speakers intervals
     * @param fileName
     *            THe name of file we are processing
     */
    public static void printSpeakerIntervals(
            ArrayList<SpeakerCluster> speakers, String fileName) {
        int idx = 0;
        for (SpeakerCluster spk : speakers) {
            idx++;
            ArrayList<Segment> segments = spk.getSpeakerIntervals();
            for (Segment seg : segments)
                System.out.println(fileName + " " + " "
                        + time(seg.getStartTime()) + " "
                        + time(seg.getLength()) + " Speaker" + idx);
        }
    }

    /**
     * @param speakers
     *            An array of clusters for which it is needed to get the
     *            speakers intervals for decoding with per-speaker adaptation
     *            with diarization.
     * @param url
     *            Url for the audio
     * @throws Exception if something went wrong
     */
    public static void speakerAdaptiveDecoding(ArrayList<SpeakerCluster> speakers,
            URL url) throws Exception {

        Configuration configuration = new Configuration();

        // Load model from the jar
        configuration
                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration
                .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration
                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
                configuration);

        TimeFrame t;
        SpeechResult result;

        for (SpeakerCluster spk : speakers) {
            Stats stats = recognizer.createStats(1);
            ArrayList<Segment> segments = spk.getSpeakerIntervals();

            for (Segment s : segments) {
                long startTime = s.getStartTime();
                long endTime = s.getStartTime() + s.getLength();
                t = new TimeFrame(startTime, endTime);

                recognizer.startRecognition(url.openStream(), t);
                while ((result = recognizer.getResult()) != null) {
                    stats.collect(result);
                }
                recognizer.stopRecognition();
            }

            Transform profile;
            // Create the Transformation
            profile = stats.createTransform();
            recognizer.setTransform(profile);

            for (Segment seg : segments) {
                long startTime = seg.getStartTime();
                long endTime = seg.getStartTime() + seg.getLength();
                t = new TimeFrame(startTime, endTime);

                // Decode again with updated SpeakerProfile
                recognizer.startRecognition(url.openStream(), t);
                while ((result = recognizer.getResult()) != null) {
                    System.out.format("Hypothesis: %s\n",
                            result.getHypothesis());
                }
                recognizer.stopRecognition();
            }
        }
    }

    public static void ProcessFile(String pathFolder, Configuration configuration) throws IOException
    {
    	File folder = new File(pathFolder);
    	int size = folder.listFiles().length;
    	
    	for(int i = 0 ; i < size;i++)
    	{
    		double start = System.currentTimeMillis();
    		File file = folder.listFiles()[i];
    		StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            InputStream stream = new FileInputStream(file);
            
            recognizer.startRecognition(stream);
            SpeechResult result;
            while ((result = recognizer.getResult()) != null) {
                WriteFile("./output/"+file.getName().substring(0,file.getName().length()-4), result.getHypothesis());
               
            }
            recognizer.stopRecognition();
            double end = System.currentTimeMillis();
            WriteFile("TimeRun", String.valueOf((end - start)/1000));
    	}
    }
    
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
    public static void WriteResultExel(String pathFResult, String pathFOutput) throws BiffException, IOException, RowsExceededException, WriteException
    {
    	int size = 0;
    	//ghi vao file exel
    	Workbook wb = Workbook.getWorkbook(new File("./rate/Static.xls"));
    	WritableWorkbook writewb = Workbook.createWorkbook(new File("./rate/Static.xls"), wb);
    	WritableSheet sheet = writewb.getSheet(0);
    	File folderResult = new File(pathFResult);
    	File folderOutput = new File(pathFOutput);
    	File TimeRun = new File("TimeRun.txt");
    	//size = folderResult.listFiles().length;
    	size = folderOutput.listFiles().length;
    	Scanner scan = new Scanner(TimeRun);
    	for(int i = 0; i < size;i++)
    	{
    		try
    		{
	    		//if(i < folderOutput.listFiles().length)
	    		{
	    			File result = folderResult.listFiles()[0];
		    		File output = folderOutput.listFiles()[i];
		    		
		    		double time = scan.nextDouble();
		    		//if(output.getName().equals(result.getName()))
		    		{
			    		sheet.addCell(new Number(0,i+2, i +1));
			    		sheet.addCell(new Label(1, i+2, output.getName().substring(0,output.getName().length()-4)));
			    		sheet.addCell(new Number(8,i+2, PercentSame(output, result)));
			    		sheet.addCell(new Label(7,i+ 2, contentFOutput.toString()));
			    		sheet.addCell(new Label(6,i+ 2, contentFresult.toString()));
			    		//sheet.addCell(new Number(8,i+2, PercentSame(output, result)));
			    		sheet.addCell(new Number(9,i+2, time));
		    		}
	    		}
    		}catch(Exception ex){}
    		contentFOutput.clear();
    		contentFresult.clear();
    	}
    	
    	writewb.write();
    	writewb.close();	
    }
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
    public static void main(String[] args) throws Exception {
    	Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        contentFOutput = new ArrayList<String>();
        contentFresult = new ArrayList<String>();
       ProcessFile("./input", configuration);
       WriteResultExel("./result", "./output");
    }
}
