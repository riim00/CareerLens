package ML.preprocessing;

import java.io.*;
import java.util.*;

public class CSVProcessor {

    public static void main(String[] args) {
        String inputFilePath = "job_offers_cleaned.csv"; // Path to the original CSV file
        String outputFilePath = "job_offers_filtered.csv"; // Path to the new CSV file

        try {
            // Read the original CSV file
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            // Write to the new CSV file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            // Read the header line
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");

            // Find the indices of the "hard_skills" and "titre" columns
            int hardSkillsIndex = -1;
            int jobTitleIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase("hard_skills")) {
                    hardSkillsIndex = i;
                } else if (headers[i].equalsIgnoreCase("titre")) {
                    jobTitleIndex = i;
                }
            }

            if (hardSkillsIndex == -1 || jobTitleIndex == -1) {
                System.err.println("Error: 'hard_skills' or 'titre' column not found in the CSV file.");
                return;
            }

            // Write the header for the new CSV file
            writer.write("titre,hard_skills");
            writer.newLine();

            // Process each line in the original CSV file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                // Extract the "hard_skills" and "titre" columns
                String jobTitle = columns[jobTitleIndex];
                String hardSkills = columns[hardSkillsIndex];

                // Write the extracted data to the new CSV file
                writer.write(jobTitle + "," + hardSkills);
                writer.newLine();
            }

            // Close the readers and writers
            reader.close();
            writer.close();

            System.out.println("New CSV file created successfully: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}