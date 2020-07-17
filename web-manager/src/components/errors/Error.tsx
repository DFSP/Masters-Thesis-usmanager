import React from "react";
import styles from './Error.module.css';

interface Props {
  message: String;
}

export const Error: React.FC<Props> = ({message}) =>
  <div className={`${styles.container}`}>
    <i className={`${styles.icon} material-icons`}>error_outline</i>
    <h5 className={`${styles.message}`}>{message}</h5>
  </div>;
