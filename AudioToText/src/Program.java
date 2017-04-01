import edu.cmu.sphinx.api.Configuration;

public class Program {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        
        ReadWriteExcel RW = new ReadWriteExcel();
        
       RW.ProcessFile("./input", configuration);
       RW.WriteResultExcel("./result", "./output", "./Information/Result.xls");
       RW.WritePeopleExcel("./input", "D:/Final1/collectionresults2/People.xls");
    }
}
