import React, { useState, useEffect, useCallback } from 'react';

interface UseLongPressProps {
  callback?: () => void;
  ms: number;
}

const UseLongPress: React.FC<UseLongPressProps> = ({children, callback = () => {}, ms = 300}) => {
  const [startLongPress, setStartLongPress] = useState(false);

  useEffect(() => {
    let timerId: NodeJS.Timeout;
    if (startLongPress) {
      timerId = setTimeout(callback, ms);
    }

    return () => {
      clearTimeout(timerId);
    };
  }, [callback, ms, startLongPress]);

  const start = useCallback(() => {
    setStartLongPress(true);
  }, []);
  const stop = useCallback(() => {
    setStartLongPress(false);
  }, []);

  return (
    <div onMouseDown={start}
         onMouseUp={stop}
         onMouseLeave={stop}
         onTouchStart={start}
         onTouchEnd={stop}>
      {children}
    </div>
  );

};

export default UseLongPress;
