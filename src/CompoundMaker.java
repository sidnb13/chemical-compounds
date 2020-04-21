import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Outputs an ionic compound given two appropriate input ions (covalent coming soon)
 */
public class CompoundMaker {

    /*A regex to filter out bogus elements, has to be hardcoded. Found from:
    https://www.johndcook.com/blog/2016/02/04/regular-expression-to-match-a-chemical-element/*/
    final static String ELEMENT_REGEX = "A[cglmrstu]|B[aehikr]?|C[adeflmnorsu]?|D[bsy]|E[rsu]" +
            "|F[elmr]?|G[ade]|H[efgos]?|I[nr]?|Kr?|L[airuv]" +
            "|M[dgnot]|N[abdeiop]?|Os?|P[abdmortu]?" +
            "|R[abefghnu]|S[bcegimnr]?|T[abcehilm]|U(u[opst])?|V|W|Xe|Yb?|Z[nr]";

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(new File("ions.csv"));
        Scanner s = new Scanner(System.in);
        in.nextLine();
        ArrayList<IonicCompound> compoundList = new ArrayList<>();
        String[] str;
        while (in.hasNextLine()) {
            str = in.nextLine().split(",");
            compoundList.add(new IonicCompound(str[3],Integer.parseInt(str[2]),str[0].equals("Polyatomic"),str[4]));
        }
        System.out.println(compoundList);
        String[] arr = new String[2];
        System.out.print("Enter the first ion (anion or cation): ");
        try {
            arr[0] = s.nextLine();
            if (findFromIons(compoundList, arr[0]).getCharge() < 0) {
                System.out.print("\nEnter the second ion (cation): ");
            } else {
                System.out.print("\nEnter the second ion (anion): ");
            }
            arr[1] = s.nextLine();
        } catch (NullPointerException n) {
            System.err.println("Please enter a valid polyatomic ion.");
        }
        for (String e : arr) {
            if (!(e.matches(ELEMENT_REGEX) || findFromIons(compoundList,e).isPolyatomic()))
                throw new Exception("Enter valid elements found on the periodic table only.");
        }
        ArrayList<IonicCompound> pairList = new ArrayList<>();
        for (String i : arr) pairList.add(findFromIons(compoundList,i));
        if (pairList.get(0).getCharge() * pairList.get(1).getCharge() > 0)
            throw new Exception("The input must contain 1 anion and cation (any input order).");
        IonicCompound anion = pairList.get(0).getCharge() < 0 ? pairList.get(0) : pairList.get(1);
        IonicCompound cation = anion.equals(pairList.get(0)) ? pairList.get(1) : pairList.get(0);
        int a = Math.abs(anion.getCharge()), c = cation.getCharge();
        if (Math.abs(a) == c) {
            System.out.println("\n"+cation.getName()+" "+anion.getName().toLowerCase()+": "+cation.getIonSpecial()+anion.getIonSpecial());
        } else {
            if (c > a) c = findLCM(a,c);
            else a = findLCM(a,c);
            String f = String.format("%s %s: %s%d%s%d",cation.getName(),anion.getName().toLowerCase(),
                    cation.isPolyatomic() && a != 1 ? "("+cation.getIonSpecial()+")" : cation.getIonSpecial(),a,
                    anion.isPolyatomic() && c != 1 ? "("+anion.getIonSpecial()+")" : anion.getIonSpecial(),c)
                    .replaceAll("1","");
            System.out.println("\n"+changeToSubscript(f));
        }
    }
    public static int findLCM(int a, int b) {
        int lcm = Math.max(a, b);
        while (lcm % a != 0 || lcm % b != 0) {
            lcm++;
        }
        return lcm;
    }
    public static String addIonParentheses(String ion) {
        ion = ion.replaceAll("[+-]","").trim();
        return String.format("(%s)",ion);
    }
    public static IonicCompound findFromIons(ArrayList<IonicCompound> arr, String ion) {
        IonicCompound res = null;
        for (IonicCompound c : arr) {
            if (c.getIonSpecial().equals(ion))
                res = c;
        }
        return res;
    }
    public static String changeToSubscript(String formula) {
        StringBuilder res = new StringBuilder();
        String[] f = formula.split("");
        for (String s : f)
            res.append(s.matches("\\d") ? (char) ('\u2080' + Integer.parseInt(s)) : s);
        return res.toString();
    }
}
