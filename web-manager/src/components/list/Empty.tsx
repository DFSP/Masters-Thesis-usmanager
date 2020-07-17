import React from "react";
import styles from './Empty.module.css';

interface Props {
  icon?: boolean;
  message?: string;
  error?: string;
}

const Empty: React.FC<Props> = ({icon, message, error}: Props) =>
  <div className={`${styles.container}`}>
    {(icon === undefined || icon) && (<i className={`${styles.icon} ${error ? 'red-text' : ''} material-icons`}>error_outline</i>)}
    <h6 className={`${styles.message}`}>{message || ''} <div className='red-text'>{error || ''}</div></h6>
  </div>;

export default Empty;
