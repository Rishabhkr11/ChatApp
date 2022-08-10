package rishabh.example.whatsapp.encryption;

public class Encrypt {

    public static String encode(String s) {
        String ini = "11111111";
        int cu = 0;

        int[] arr = new int[11111111];

        for (int i = 0; i < s.length(); i++) {
            arr[i] = s.charAt(i);
            cu++;
        }
        String res = "";

        int[] bin = new int[111];
        int idx = 0;

        for (int i1 = 0; i1 < cu; i1++) {

            int temp = arr[i1];

            for (int j = 0; j < cu; j++) bin[j] = 0;
            idx = 0;

            while (temp > 0) {
                bin[idx++] = temp % 2;
                temp = temp / 2;
            }
            String dig = "";
            String temps;

            for (int j = 0; j < 7; j++) {

                temps = Integer.toString(bin[j]);

                dig = dig.concat(temps);
            }
            String revs = "";

            for (int j = dig.length() - 1; j >= 0; j--) {
                char ca = dig.charAt(j);
                revs = revs.concat(String.valueOf(ca));
            }
            res = res.concat(revs);
        }
        res = ini.concat(res);
        return res;
    }
}
