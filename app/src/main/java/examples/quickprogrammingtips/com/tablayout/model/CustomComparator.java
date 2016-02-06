package examples.quickprogrammingtips.com.tablayout.model;

import java.util.Comparator;

public class CustomComparator implements Comparator<File> {
    @Override
    public int compare(File o1, File o2) {
        if (o1 instanceof Mp3File)
            if (o2 instanceof Mp3File){
                Mp3File o11 = (Mp3File) o1;
                Integer m1= o11.getTracknr();
                Mp3File o21 = (Mp3File) o2;
                Integer m2= o21.getTracknr();
                if (m1!=m2)
                    return (m1.compareTo(m2));
                else return (o11.getTitle()).compareTo(o21.getTitle());
            } else return 1;
        else if(o2 instanceof Mp3File) return 0;
        else
            return o1.getFname().compareTo(o2.getFname());
    }
}
