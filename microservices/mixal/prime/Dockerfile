FROM node:boron

# Create app directory
RUN mkdir -p /usr/src/prime_sum_app
WORKDIR /usr/src/prime_sum_app

# Install app dependencies
COPY package.json /usr/src/prime_sum_app/
RUN npm install
# Bundle app source
COPY . /usr/src/prime_sum_app
EXPOSE 9001
CMD [ "node", "index.js"]