package uwu.misaka.bot.crossdress;

import java.util.ArrayList;

public class CDEmbed {
    public String header;
    public String footer;
    public String authorURL;
    public String image;
    public ArrayList<CDField> fields = new ArrayList<>();

    public static class CDField {
        String key;
        String value;
        boolean inline = false;

        public CDField(String header, String footer) {
            this.key = header;
            this.value = footer;
        }

        public CDField(String header, String footer, boolean inline) {
            this.key = header;
            this.value = footer;
            this.inline = inline;
        }
    }
}
