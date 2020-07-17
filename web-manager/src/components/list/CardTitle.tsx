import React from "react";
import styles from './CardTitle.module.css';
import {capitalize} from "../../utils/text";

interface CardTitleProps {
  title: string;
}

const CardTitle: React.FC<CardTitleProps> = ({title}) =>
  <h6 className={`${styles.cardTitle}`}>{capitalize(title)}</h6>;

export default CardTitle;