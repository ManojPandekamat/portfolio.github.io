const AWS = require('aws-sdk');

AWS.config.update({
    region: 'us-east-1' 
});
const docClient = new AWS.DynamoDB.DocumentClient();

const queryTable = async () => {
    const params = {
        TableName: 'Movies',
        KeyConditionExpression: '#yr = :yyyy',
        ExpressionAttributeNames: {
            '#yr': 'year'
        },
        ExpressionAttributeValues: {
            ':yyyy': 2024
        }
    };

    try {
        const data = await docClient.query(params).promise();
        console.log('Query succeeded:', data.Items);
    } catch (err) {
        console.error('Unable to query. Error:', JSON.stringify(err, null, 2));
    }
};

(async () => {
    await queryTable();
})();
