import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {
    static Model model = ModelFactory.createDefaultModel();
    public static void main(String[] args) throws ParseException {
        // create an empty model
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open( "A1_Semanticv3.owl" );
        if (in == null) {
            throw new IllegalArgumentException("File: not found");
        }
        // read the RDF/XML file
        model.read(in, null);
        String patientName = "Nada";
        listOfMedicationsAndDiseases(patientName);
        alert(patientName);
    }

    private static void listOfMedicationsAndDiseases(String patientName) {
        String patientURL = "http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#" + patientName;
        Resource patient = model.getResource(patientURL);
        Property diseaseProperty = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#HasDisease");
        Property diseaseName = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#DiseaseName");
        Property medicationProperty = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#TakeMedication");
        Property medicationName = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#MedicationName");

        StmtIterator stmtIterator = patient.listProperties(diseaseProperty);
        System.out.println("Diseases: ");
        while(stmtIterator.hasNext()){
            System.out.println(stmtIterator.nextStatement().getProperty(diseaseName).getString());
        }
        System.out.println("");

        stmtIterator = patient.listProperties(medicationProperty);
        System.out.println("Medications Taken: ");
        while(stmtIterator.hasNext()){
            System.out.println(stmtIterator.nextStatement().getProperty(medicationName).getString());
        }
        System.out.println("");

    }

    private static void alert(String patientName) throws ParseException {
        System.out.println("");
        ArrayList<Date> startDate = new ArrayList<>();
        ArrayList<Date> endDate = new ArrayList<>();
        ArrayList<String> medicineName = new ArrayList<>();
        String patientURL = "http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#" + patientName;
        Resource patient = model.getResource(patientURL);
        Property medicationProperty = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#TakeMedication");
        Property medicationName = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#MedicationName");

        StmtIterator stmtIterator = patient.listProperties(medicationProperty);
        while(stmtIterator.hasNext()){
            Resource medicine = stmtIterator.nextStatement().getProperty(medicationName).getSubject();
            StmtIterator stmtIterator1 = medicine.listProperties();
            for (int i = 0; i < 3; i++)
            {
                String statment = stmtIterator1.nextStatement().getString();
                //System.out.print(statment);
                if(i==1)
                {
                    medicineName.add(statment);
                    continue;
                }
                SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy");
                Date date=formatter.parse(statment);
                if(i==0)
                    startDate.add(date);
                else if(i==2)
                    endDate.add(date);
            }
        }

        boolean areOverlapping = false;
        while (!medicineName.isEmpty()) {
            for (int i = 0; i < medicineName.size(); i++) {
                for (int j = 0; j < medicineName.size(); j++) {
                    if (i != j)
                        areOverlapping = checkOverlap(startDate.get(i), endDate.get(i), startDate.get(j), endDate.get(j));
                    if (areOverlapping) {
                        System.out.println("2 Medicines are overlapping: " + medicineName.get(i) + " and " + medicineName.get(j));
                        areOverlapping = false;
                        if (checkInteraction(medicineName.get(i), medicineName.get(j), "Major"))
                            System.out.println(medicineName.get(i) + " Has Major Interaction On " + medicineName.get(j));
                        else if (checkInteraction(medicineName.get(i), medicineName.get(j), "Moderate"))
                            System.out.println(medicineName.get(i) + " Has Moderate Interaction On " + medicineName.get(j));
                        else if (checkInteraction(medicineName.get(i), medicineName.get(j), "Minor"))
                            System.out.println(medicineName.get(i) + " Has Minor Interaction On " + medicineName.get(j));
                        else
                            System.out.println("Has no interaction");
                        System.out.println("");
                    }
                }
                medicineName.remove(0);
            }
        }
    }

    private static boolean checkInteraction(String m1, String m2, String interactionSeverity)
    {
        String medicineURL = "http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#"+m1;
        Resource medicine1 = model.getResource(medicineURL);
        Property hasInteractionOn = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#" + interactionSeverity);
        StmtIterator stmtIterator = medicine1.listProperties(hasInteractionOn);
        while (stmtIterator.hasNext()) {
            Resource medicine2 = model.getResource(String.valueOf(stmtIterator.nextStatement().getResource()));
            Property medicationName = model.createProperty("http://www.semanticweb.org/eyad/ontologies/2021/11/untitled-ontology-4#MedicationName");
            StmtIterator stmtIterator1 = medicine2.listProperties(medicationName);
            String checkMedicine = stmtIterator1.nextStatement().getString();
            if(m2.equalsIgnoreCase(checkMedicine))
            {
                return true;
            }
        }
        return false;
    }

  private static boolean checkOverlap(Date start_time1, Date end_time1, Date start_time2, Date end_time2)
  {
      return (end_time2.after(start_time1) && end_time1.after(start_time2) ? true : false);
  }
}