import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const ScrollToTop: React.FC = () => {
  const { pathname } = useLocation();

  useEffect(() => {
    // Scroll to top when route changes
    if (typeof window !== 'undefined' && window.scrollTo) {
      window.scrollTo({
        top: 0,
        left: 0,
        behavior: 'smooth',
      });
    } else if (typeof window !== 'undefined' && window.scroll) {
      // Fallback for older browsers
      window.scroll(0, 0);
    }
  }, [pathname]);

  return null;
};

export default ScrollToTop;

