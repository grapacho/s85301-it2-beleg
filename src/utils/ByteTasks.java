package utils;

public class ByteTasks {

	/* suche im array data von position start bis ende nach dem ersten vorkommen des arrays gesucht
     * gibt die position der ersten kompletten uebereinstimmung aus
     */
    public int indexOf( byte[] data, int start, int ende, byte[] gesucht) {
        if( data == null || gesucht == null) return -1;

        int[] failure = new int[gesucht.length];
        
        int j = 0;
        for (int i = 1; i < gesucht.length; i++) {
            while (j>0 && gesucht[j] != gesucht[i]) {
                j = failure[j - 1];
            }
            if (gesucht[j] == gesucht[i]) {
                j++;
            }
            failure[i] = j;
        }
        //--> bspw: FFD8 -> failure{0,1,0,0}
        // FFF0 -> failure{0,1,2,0} | ABCD -> {0,0,0,0}
        // --> beschleunigt suche nach byte[] gesucht
        
        j = 0;

        for( int i = start; i < ende; i++) {
            while (j > 0 && (gesucht[j] != '*' && gesucht[j] != data[i])) {
                j = failure[j - 1];
            }
            if (gesucht[j] == '*' || gesucht[j] == data[i]) {
                j++;
            }
            if (j == gesucht.length) {
                return i - gesucht.length + 1;
            }
        }
        return -1;
    }

   
    
    public int byteArrayToInt(byte[] b) 
    {
    	int a=0;
    	for(int i=0;i<b.length;i++){
    		a = a | ((b[i] & 0xFF) << ((b.length-1)-i)*8);
    	}
    		
        return  a;
    }
    
    public byte[] intToByteArray(int a)
    {
        return new byte[] {
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),   
            (byte) ((a >> 8) & 0xFF),   
            (byte) (a & 0xFF)
        };
    }
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}