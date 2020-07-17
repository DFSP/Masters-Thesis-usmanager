import React, { useState, useEffect } from 'react';
import { useTransition, animated } from 'react-spring';
//import shuffle from 'lodash/shuffle'

interface AnimatedListProps<T> {
  list: T[];
  show: (element: T, index: number, last: boolean) => JSX.Element;
  header?: () => JSX.Element;
}

export default function AnimatedList<T>(props: AnimatedListProps<T>) {
  const {list, show, header} = props;
  const keyedList = list.map((item, index) => ({key: index, item}));
  const [rows, setList] = useState(keyedList);

  useEffect(() => setList(keyedList),[list, keyedList]);
  /*useEffect(() => void setInterval(() => setList(shuffle), 2000), [])*/

  let height = 0;
  const transitions = useTransition(
    rows.map(data => ({ data, height: 32.4, y: (height += 32.4) - 32.4 })),
    item => item.data.key,
    {
      from: { height: 0, opacity: 0 },
      leave: { height: 0, opacity: 0 },
      enter: ({ y, height }) => ({ y, height, opacity: 1 }),
      update: ({ y, height }) => ({ y, height })
    }
  );
  //FIXME: currently not being used, has a few bugs
  return (
    <div style={{height: (rows.length * 59) }}>
      {header && header()}
      {transitions.map(({item, props: { y, ...rest}, key}, index) => (
        <animated.div
          key={key}
          style={{
            zIndex: list.length - index,
            transform: y.interpolate((y: number) => `translate3d(0, ${y}px, 0)`), ...rest
          }}>
          {show(item.data.item, index, index === list.length - 1)}
        </animated.div>
      ))}
    </div>
  )
}