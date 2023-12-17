import java.io.File;
import java.io.IOException;

public class Main
{
    /*
        Ordner von "dataset/synthetic_images/200x200"
        in lokalen ordner /img/source kopieren
        
        initiales ausführen erstellt alle benötigten ordner
     */
    public static void main(String[] args) throws IOException
    {
        String pathInBeforePose = "img/source/pose";
        String pathOutBeforePoseShirtPants = "img/shirt_pants/pose";
        String pathOutBeforePoseStripes = "img/stripes/pose";
        String pathMale = "/male/subject_mesh_";
        String pathFemale = "/female/subject_mesh_";
    
        new File("img/source").mkdirs();
    
        new File("img/shirt_pants/pose0/male").mkdirs();
        new File("img/shirt_pants/pose1/male").mkdirs();
        new File("img/shirt_pants/pose0/female").mkdirs();
        new File("img/shirt_pants/pose1/female").mkdirs();
    
        new File("img/stripes/pose0/male").mkdirs();
        new File("img/stripes/pose1/male").mkdirs();
        new File("img/stripes/pose0/female").mkdirs();
        new File("img/stripes/pose1/female").mkdirs();
        
        for(int i = 1; i <= 1; i++)
        {
            String ending = String.format("%04d", i) + ".png";
            
            Clothier clothier = new Clothier();
            clothier.putOnShirtPants(new File(pathInBeforePose + "0" + pathMale + ending), pathOutBeforePoseShirtPants + "0" + pathMale + ending);
            clothier.putOnShirtPants(new File(pathInBeforePose + "1" + pathMale + ending), pathOutBeforePoseShirtPants + "1" + pathMale + ending);
            clothier.putOnShirtPants(new File(pathInBeforePose + "0" + pathFemale + ending), pathOutBeforePoseShirtPants + "0" + pathFemale + ending);
            clothier.putOnShirtPants(new File(pathInBeforePose + "1" + pathFemale + ending), pathOutBeforePoseShirtPants + "1" + pathFemale + ending);
    
            clothier.putOnStripes(new File(pathInBeforePose + "0" + pathMale + ending), pathOutBeforePoseStripes + "0" + pathMale + ending, 5, 15);
            clothier.putOnStripes(new File(pathInBeforePose + "1" + pathMale + ending), pathOutBeforePoseStripes + "1" + pathMale + ending, 5, 15);
            clothier.putOnStripes(new File(pathInBeforePose + "0" + pathFemale + ending), pathOutBeforePoseStripes + "0" + pathFemale + ending, 5, 15);
            clothier.putOnStripes(new File(pathInBeforePose + "1" + pathFemale + ending), pathOutBeforePoseStripes + "1" + pathFemale + ending, 5, 15);
        }
    }
}
