public class IonicCompound {
    private final String name;
    private final int charge;
    private final boolean polyatomic;
    private String ion;

    public IonicCompound(String n, int c, boolean isP, String io) {
        name = n;
        charge = c;
        polyatomic = isP;
        ion = io;
    }

    public String getName() {
        return name;
    }

    public int getCharge() {
        return charge;
    }

    public String getIon() { return ion; }

    public String getIonSpecial() {
        int i = ion.length()-1;
        if (Character.isDigit(ion.charAt(ion.length()-2))) {
            for (i = ion.length() - 1; i >= 0; i--) {
                if (String.valueOf(ion.charAt(i)).matches("\\d"))
                    break;
            }
        }
        return ion.substring(0,i).trim();
    }

    public void setIon(String s) {
        ion = s;
    }

    public boolean isPolyatomic() {
        return polyatomic;
    }

    public boolean isAnion() {
        return charge < 0;
    }

    public String toString() {
        return name+" "+getIonSpecial()+" "+charge;
    }
}
