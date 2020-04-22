public class IonicCompound {
    private final String ion;
    private final int charge;
    private final boolean polyatomic;
    private String name;

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

    public void setName(String n) {
        name = n;
    }

    /**
     * @return a trimmed down version of ionic formula without charge suffix
     */
    public String getIonSpecial() {
        int i = ion.length() - 1;
        if (Character.isDigit(ion.charAt(ion.length() - 2))) {
            for (i = ion.length() - 1; i >= 0; i--) {
                if (String.valueOf(ion.charAt(i)).matches("\\d"))
                    break;
            }
        }
        return ion.substring(0, i).trim();
    }

    public boolean isPolyatomic() {
        return polyatomic;
    }

    public String toString() {
        return String.format("%s %s %d", name, getIonSpecial(), charge);
    }
}
