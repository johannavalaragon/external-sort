/**
 * External Sort Project main method and support methods:
 */

import java.io.*;

/**
 * The class containing the main method.
 *
 * @author johanna
 * @version 04.08.2026
 */

//On my honor:
//
//- I have not used source code obtained from another current or
//former student, or any other unauthorized source, either
//modified or unmodified.
//
//- All source code and documentation used in my program is either my
//original work, or was derived by me from the source code
//published in the textbook for this course. I understand that I am
//permitted to use an LLM tool to assist me with writing project
//code, under the condition that I submit with the project a full
//transcript of my interactions with the LLM (showing my prompts and
//the LLM's response). I understand that I am responsible for being
//able to complete this work without the use of LLM assistance.
//
//- I have not discussed coding details about this project with
//anyone other than my partner (in the case of a joint
//submission), instructor, ACM/UPE tutors or the TAs assigned
//to this course. I understand that I may discuss the concepts
//of this program with other students, and that another student
//may help me debug my program so long as neither of us writes
//anything during the discussion or modifies any computer file
//during the discussion. I have violated neither the spirit nor
//letter of this restriction.


public class ExternalSortProj
{

    /**
     * Main: Process input parameters
     *
     * @param args
     *            The command line parameters
     * @throws IOException
     */
    public static void main(String[] args)
        throws IOException
    {
        if (args.length != 1)
        {
            System.out.println(
                "Usage: ExernalSortProj <data-file-name>");
            return;
        }
        File temp = new File(args[0]);
        if (!temp.exists())
        {
            System.out.println("There is no such input file as |" + args[0]
                + "|");
            return;
        }
        long time1 = System.currentTimeMillis();
        ExternalSort.sort(args[0]);
        long time2 = System.currentTimeMillis();
        System.out.println("Time is " + (time2 - time1));
    }
}
