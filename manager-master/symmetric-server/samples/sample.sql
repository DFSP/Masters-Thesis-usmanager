CREATE TABLE apps ( 
   id INT NOT NULL, 
   name VARCHAR(50) NOT NULL,
   PRIMARY KEY (id)
);

CREATE TABLE rules (
  id INT NOT NULL,
  worker_id VARCHAR(5) NOT NULL,
  name VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
);