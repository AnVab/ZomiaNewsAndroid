package news.zomia.zomianews.customcontrols;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class DirectionalViewPager  extends ViewPager {

    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_BOTH = 3;

    public interface DirectionProvider {
        public int getScrollDirections(int position);
    }

    public DirectionalViewPager(Context context) {
        super(context);
    }
    public DirectionalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        final PagerAdapter adapter = getAdapter();
        final int page = getCurrentItem();

        if (adapter == null || page >= adapter.getCount() || !(adapter instanceof DirectionProvider)) {
            return super.canScroll(v, checkV, dx, x, y);
        }

        final DirectionProvider provider = (DirectionProvider) adapter;
        final int directions = provider.getScrollDirections(page);

        switch (directions) {
            case DIRECTION_NONE : {
                return true;
            }

            case DIRECTION_LEFT : {
                return dx < 0 || super.canScroll(v, checkV, dx, x, y);
            }

            case DIRECTION_RIGHT : {
                return dx > 0 || super.canScroll(v, checkV, dx, x, y);
            }

            case DIRECTION_BOTH : {
                return super.canScroll(v, checkV, dx, x, y);
            }

            default : {
                throw new IllegalArgumentException("Unknown directions value : " + directions);
            }
        }
    }
}
