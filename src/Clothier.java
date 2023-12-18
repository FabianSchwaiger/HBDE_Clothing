import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Clothier
{
    private static final int darkestBackground = 232;
    private BufferedImage image;
    private String outPath;
    private int halfWidth, halfHeight;
    private int headTop, neck, crotchTop;
    
    public Clothier()
    {
    
    }
    
    public void putOnShirtPants(File sourceImage, String outPath) throws IOException
    {
        image = ImageIO.read(sourceImage);
        this.outPath = outPath;
        halfWidth = image.getHeight() / 2;
        halfHeight = image.getWidth() / 2;
        
        // IntTuple res = findDarkestAndBrightestNonBackgroundPixel();
        
        headTop = findHeadTop();
        neck = findNeck();
        crotchTop = (find_crotch_top() - 6);
        int chestHeight = (int) ((crotchTop - neck) * 1.1);
        
        recolor(halfWidth - chestHeight, halfWidth + chestHeight, neck, crotchTop, RecolorFunction.DARKER1.func);
        recolor(halfWidth - chestHeight, halfWidth + chestHeight, crotchTop, image.getHeight(), RecolorFunction.LIGHTER1.func);
        
        ImageIO.write(image, "png", new File(outPath));
    }
    
    public void putOnStripes(File sourceImage, String outPath, int minThickness, int maxThickness) throws IOException
    {
        image = ImageIO.read(sourceImage);
        outPath = outPath;
        halfWidth = image.getHeight() / 2;
        halfHeight = image.getWidth() / 2;
        
        // IntTuple res = findDarkestAndBrightestNonBackgroundPixel();
        
        headTop = findHeadTop();
        neck = findNeck();
        crotchTop = (find_crotch_top() - 6);
        int chestHeight = (int) ((crotchTop - neck) * 1.1);
        
        int start = neck;
        boolean flag = false;
        Random random = new Random();
        while(start < image.getHeight())
        {
            int to = Math.min(random.nextInt(minThickness, maxThickness) + start, image.getHeight());
            recolor(halfWidth - chestHeight, halfWidth + chestHeight, start, to, flag ? this::darker : this::brighter);
            flag = !flag;
            start = to + 1;
        }
        
        ImageIO.write(image, "png", new File(outPath));
    }
    
    private IntTuple findDarkestAndBrightestNonBackgroundPixel()
    {
        int darkest = 255;
        int brightest = 0;
        
        for(int i = 0; i < image.getWidth(); i++)
        {
            for(int j = 0; j < image.getHeight(); j++)
            {
                int p = image.getRGB(i, j) & 0xff;
                if(isBackground(p))
                {
                    darkest = Math.min(darkest, p);
                    brightest = Math.max(brightest, p);
                }
            }
        }
        return new IntTuple(darkest, brightest);
    }
    
    private int findHeadTop()
    {
        int top = 0;
        int bottom = image.getHeight() / 4;
        
        while(top != bottom)
        {
            int middle = (top + bottom) / 2;
            
            // still on background -> down
            if((image.getRGB(halfWidth, middle) & 0xff) >= darkestBackground)
            {
                top = middle + 1;
            }
            else
            {
                bottom = middle;
            }
        }
        return top;
    }
    
    private int findNeck()
    {
        int pos_x = halfWidth;
        int pos_y = headTop;
        
        int p = (image.getRGB(pos_x, pos_y) & 0xff);
        
        // right until ear
        while(!isBackground(p))
        {
            while(!isBackground(p))
            {
                pos_x++;
                p = (image.getRGB(pos_x, pos_y) & 0xff);
            }
            pos_x--;
            pos_y++;
            p = (image.getRGB(pos_x, pos_y) & 0xff);
        }
        
        // left down until neck start
        while(isBackground(p))
        {
            while(isBackground(p))
            {
                pos_x--;
                p = (image.getRGB(pos_x, pos_y) & 0xff);
            }
            pos_x++;
            pos_y++;
            p = (image.getRGB(pos_x, pos_y) & 0xff);
        }
        pos_y--;
        
        return pos_y;
    }
    
    // TODO only finds crotch approximately
    private int find_crotch_top()
    {
        int darkestY = halfHeight;
        int darkestValue = 255;
        for(int posY = image.getHeight() / 2; posY < image.getHeight(); posY++)
        {
            int p = (image.getRGB(halfWidth - 1, posY) & 0xff);
            if(p < darkestValue)
            {
                darkestValue = p;
                darkestY = posY;
            }
            if(isBackground(p))
            {
                break;
            }
        }
        return darkestY;
    }
    
    
    private void recolor(int fromVertical, int toVertical, int fromHorizontal, int toHorizontal, Function<Integer, Integer> recolorFactorFunction)
    {
        IntStream.range(fromHorizontal, toHorizontal).parallel().forEach(j ->
        {
            for(int i = fromVertical; i < toVertical; i++)
            {
                int p = image.getRGB(i, j);
                int a = (p >> 24) & 0xff;
                int r = p & 0xff;
                
                if(!isBackground(r))
                {
                    int transformed;
                    
                    transformed = recolorFactorFunction.apply(r);
                    
                    image.setRGB(i, j, (a << 24) | (transformed << 16) | (transformed << 8) | transformed);
                }
            }
        });
    }
    
    private static int transformColor(int i, double factor)
    {
        if(factor >= 1.0)
        {
            return (int) Math.min(i * factor, 255);
        }
        else
        {
            return (int) Math.max(i * factor, 0);
        }
    }
    
    private static boolean isBackground(int color)
    {
        return color >= darkestBackground;
    }
    
    private record IntTuple(int x, int y) {}
    
    public enum RecolorFunction
    {
        LIGHT(x -> transformColor(x, (-0.00354978 * x + 1.8589604))),
        DARK(x -> transformColor(x, (255.0 / x))),
        LIGHTER1(x -> lighter2(x, 2)),
        LIGHTER(x -> lighter3(x, 2, 3)),
        DARKER1(x -> darker2(x, 2)),
        DARKER(x -> darker3(x, 2, 3));
        
        private final Function<Integer, Integer> func;
        
        RecolorFunction(Function<Integer, Integer> func)
        {
            this.func = func;
        }
        
        public Function<Integer, Integer> getFunc() {
            return func;
        }
    }
    
    private static int lighter3(int c, int baseFactor, int lightFactor)
    {
        return (int)Math.ceil((c * baseFactor + 255D * lightFactor) / (baseFactor + lightFactor));
    }
    
    private static int lighter2(int c, int factor)
    {
        return lighter3(c, 1, factor);
    }
    
    private static int darker3(int c, int baseFactor, int darkFactor)
    {
        return (int)Math.ceil((c * baseFactor + 0D * darkFactor) / (baseFactor + darkFactor));
    }
    
    private static int darker2(int c, int factor)
    {
        return darker3(c, 1, factor);
    }
    
    private Integer brighter(int x)
    {
        return transformColor(x, (-0.00354978 * x + 1.8589604));
    }
    
    private Integer darker(int x)
    {
        return transformColor(x, (x / 255.0));
    }
    
}
