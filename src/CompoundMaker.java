import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Outputs an ionic compound given two appropriate input ions
 * Also can output a covalent compound or its name depending on input
 */
public class CompoundMaker {

    /*A regex to filter out bogus elements, has to be hardcoded. Found from:
    https://www.johndcook.com/blog/2016/02/04/regular-expression-to-match-a-chemical-element/*/
    final static String ELEMENT_REGEX = "A[cglmrstu]|B[aehikr]?|C[adeflmnorsu]?|D[bsy]|E[rsu]" +
            "|F[elmr]?|G[ade]|H[efgos]?|I[nr]?|Kr?|L[airuv]" +
            "|M[dgnot]|N[abdeiop]?|Os?|P[abdmortu]?" +
            "|R[abefghnu]|S[bcegimnr]?|T[abcehilm]|U(u[opst])?|V|W|Xe|Yb?|Z[nr]";

    private static LinkedHashMap<String, String> elements;
    private static LinkedHashMap<String, String> suffixes;
    private static LinkedHashMap<String, Integer> prefixes;

    /**
     * Performs task of generating the compound
     * @throws Exception for custom errors
     */
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        Scanner in;
        System.out.print("Ionic or covalent? [1/2]: ");
        if (s.nextLine().equals("2")) {
            in = new Scanner(new File("elements_1.csv"));
            elements = new LinkedHashMap<>();
            while (in.hasNextLine()) {
                String[] arr = in.nextLine().split(",");
                elements.put(arr[1].toLowerCase(), arr[2]);
            }
            Scanner pix = new Scanner(new File("prefixes.csv"));
            Scanner six = new Scanner(new File("suffixes.csv"));
            suffixes = new LinkedHashMap<>();
            while (six.hasNextLine()) {
                String[] e = six.nextLine().split(",");
                suffixes.put(e[0], e[1]);
            }
            prefixes = new LinkedHashMap<>();
            while (pix.hasNextLine()) {
                String[] e = pix.nextLine().split(",");
                prefixes.put(e[0], Integer.parseInt(e[1]));
            }
            System.out.print("\nEnter either a covalent compound or its name: ");
            String input = s.nextLine();
            if (input.replaceAll("\\d", "").length() == input.length()) {
                String[] arr = input.split("\\s");
                System.out.println(changeToSubscript(getTentativeFormula(arr[0]) + getTentativeFormula(arr[1])));
            } else if (!input.contains(" ")) {
                int i;
                for (i = 0; i < input.length(); i++) {
                    if (Character.isDigit(input.charAt(i)))
                        break;
                }
                var endIndex = i == input.length() ? 1 : i + 1;
                String[] arr = {input.substring(0, endIndex), input.substring(endIndex)};
                StringBuilder res = new StringBuilder();
                for (String e : arr) {
                    res.append(getNameFromFormula(e.replaceAll("\\d+", ""),
                            e.matches(".*\\d+.*") ? Integer.parseInt(e.replaceAll("[A-Za-z]", "")) : 1,
                            e.equals(arr[1]))).append(" ");
                }
                res = new StringBuilder(res.toString().replaceAll("1", "").replaceAll("oo", "o").trim());
                res = new StringBuilder(res.substring(0, 1).toUpperCase() + res.substring(1));
                System.out.println("\n" + res);
            }
        } else {
            in = new Scanner(new File("ions.csv"));
            in.nextLine();
            ArrayList<IonicCompound> compoundList = new ArrayList<>();
            String[] str;
            while (in.hasNextLine()) {
                str = in.nextLine().split(",");
                compoundList.add(new IonicCompound(str[3], Integer.parseInt(str[2]), str[0].equals("Polyatomic"), str[4]));
            }
            String[] arr = new String[2];
            System.out.print("Enter the first ion (anion or cation): ");
            try {
                arr[0] = s.nextLine();
                System.out.print("\nEnter the second ion (" +
                        (findFromIons(compoundList, arr[0]).getCharge() < 0 ? "cation" : "anion") + "): ");
                arr[1] = s.nextLine();
            } catch (NullPointerException n) {
                System.err.println("Please enter a valid polyatomic or regular ion.");
            }
            for (String e : arr) {
                if (!(e.matches(ELEMENT_REGEX) || findFromIons(compoundList, e).isPolyatomic()))
                    throw new Exception("Enter valid elements found on the periodic table only.");
            }
            ArrayList<IonicCompound> pairList = new ArrayList<>();
            for (String i : arr) pairList.add(findFromIons(compoundList, i));
            if (pairList.get(0).getCharge() * pairList.get(1).getCharge() > 0)
                throw new Exception("The input must contain 1 anion and cation (any input order).");
            IonicCompound anion = pairList.get(0).getCharge() < 0 ? pairList.get(0) : pairList.get(1);
            IonicCompound cation = anion.equals(pairList.get(0)) ? pairList.get(1) : pairList.get(0);
            if (cation.getName().contains("(")) {
                System.out.print("\nEnter the special oxidation case for your chosen transition metal." +
                        "\nThis must be in nomenclature format (Ex. Iron(III)): ");
                cation = findFromName(compoundList, s.nextLine());
            }
            int a = Math.abs(anion.getCharge()), c = cation.getCharge();
            if (Math.abs(a) == c) {
                System.out.println("\n" + changeToSubscript(cation.getName() + " " +
                        anion.getName().toLowerCase() + ": " +
                        cation.getIonSpecial() + anion.getIonSpecial()));
            } else {
                if (c > a) c = findLCM(a, c);
                else a = findLCM(a, c);
                String f = String.format("%s %s is %s%d%s%d", cation.getName(), anion.getName().toLowerCase(),
                        cation.isPolyatomic() && a != 1 ? "(" + cation.getIonSpecial() + ")" : cation.getIonSpecial(), a,
                        anion.isPolyatomic() && c != 1 ? "(" + anion.getIonSpecial() + ")" : anion.getIonSpecial(), c)
                        .replaceAll("1", "");
                System.out.println("\n" + changeToSubscript(f));
            }
        }
    }

    /**
     * @param name is part of the formula's English name
     * @return the partial formula determined
     */
    public static String getTentativeFormula(String name) {
        int freq = 1, breakPoint = 0;
        String element = "";
        name = name.toLowerCase();
        for (Map.Entry<String, Integer> e : prefixes.entrySet()) {
            if (name.contains(e.getKey())) {
                freq = e.getValue();
                breakPoint = e.getKey().length();
            }
        }
        for (Map.Entry<String, String> e : suffixes.entrySet()) {
            if (name.substring(breakPoint).equals(e.getKey()))
                element = e.getValue();
        }
        if (element.isBlank()) {
            for (Map.Entry<String, String> e : elements.entrySet()) {
                if (name.substring(breakPoint).equals(e.getKey()))
                    element = e.getValue();
            }
        }
        return freq == 1 ? element : element + freq;
    }

    public static String getNameFromFormula(String element, int count, boolean isLast) {
        StringBuilder name = new StringBuilder();
        for (Map.Entry<String, Integer> e : prefixes.entrySet()) {
            if (e.getValue() == count)
                name.append(e.getKey());
        }
        for (Map.Entry<String, String> e : isLast ? suffixes.entrySet() : elements.entrySet()) {
            if (e.getValue().equals(element))
                name.append(e.getKey());
        }
        return name.toString();
    }

    /**
     * @param a an int
     * @param b an int
     * @return the lcm of parameters
     */
    public static int findLCM(int a, int b) {
        int lcm = Math.max(a, b);
        while (lcm % a != 0 || lcm % b != 0) {
            lcm++;
        }
        return lcm;
    }

    /**
     * @param arr the master list of ions
     * @param ion the ion formula name to find
     * @return the IonicCompound object represented by String ion
     */
    public static IonicCompound findFromIons(ArrayList<IonicCompound> arr, String ion) {
        IonicCompound res = null;
        for (IonicCompound c : arr) {
            if (c.getIonSpecial().equals(ion))
                res = c;
        }
        return res;
    }

    /**
     * @param arr  the compound list
     * @param name name of ion in formal nomenclature
     * @return the ionic compound object representing this name
     */
    public static IonicCompound findFromName(ArrayList<IonicCompound> arr, String name) {
        IonicCompound res = null;
        for (IonicCompound c : arr) {
            if (c.getName().equals(name))
                res = c;
        }
        return res;
    }

    /**
     * @param formula the formula to change
     * @return the formula with all numbers as subscripts
     */
    public static String changeToSubscript(String formula) {
        StringBuilder res = new StringBuilder();
        String[] f = formula.split("");
        for (String c : f)
            res.append(c.matches("\\d+") ? (char) ('\u2080' + Integer.parseInt(c)) : c);
        return res.toString();
    }
}
