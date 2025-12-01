-- WebSocket support schema updates
-- Add protocol column to auto_case table
ALTER TABLE auto_case ADD COLUMN IF NOT EXISTS protocol VARCHAR(20) DEFAULT 'http';

-- Create WebSocket configuration table
CREATE TABLE IF NOT EXISTS auto_websocket_config (
    id SERIAL PRIMARY KEY,
    case_id INTEGER REFERENCES auto_case(id) ON DELETE CASCADE,
    ws_url VARCHAR(500),
    heartbeat_interval INTEGER DEFAULT 30000,
    message_timeout INTEGER DEFAULT 5000,
    connection_timeout INTEGER DEFAULT 30000,
    max_reconnect_attempts INTEGER DEFAULT 3,
    channels TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for WebSocket cases
CREATE INDEX IF NOT EXISTS idx_protocol_auto_case ON auto_case(protocol);
CREATE INDEX IF NOT EXISTS idx_case_id_websocket_config ON auto_websocket_config(case_id);

-- Insert WebSocket test cases
-- 1. Heartbeat test
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, false, 'true', 'websocket_market', 'heartbeat',
    'WS-001', 'Test WebSocket heartbeat mechanism', 'websocket,heartbeat',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "messages": [
            {
                "type": "send",
                "content": {
                    "id": 1587523073344,
                    "method": "public/heartbeat",
                    "code": 0
                }
            },
            {
                "type": "expect",
                "timeout": 5000,
                "validate": {
                    "method": "public/heartbeat",
                    "code": 0
                }
            }
        ],
        "heartbeat": {
            "message": {
                "id": 1587523073344,
                "method": "public/heartbeat",
                "code": 0
            },
            "interval": 30000
        }
    }'
);

-- 2. User Order Channel Subscription
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, false, 'true', 'websocket_market', 'user_order_subscription',
    'WS-002', 'Test user.order channel subscription', 'websocket,subscription,order',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "channels": ["user.order"],
        "messages": [
            {
                "type": "subscribe",
                "content": {
                    "id": 1,
                    "method": "subscribe",
                    "params": {
                        "channels": ["user.order"]
                    },
                    "nonce": 1587523073344
                },
                "expectConfirmation": true
            },
            {
                "type": "expect",
                "timeout": 10000,
                "validate": {
                    "method": "${any}",
                    "params": {
                        "channel": "user.order"
                    }
                }
            }
        ],
        "validation": {
            "messageOrder": false,
            "schemaValidation": true
        }
    }'
);

-- 3. User Balance Channel Subscription
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, false, 'true', 'websocket_market', 'user_balance_subscription',
    'WS-003', 'Test user.balance channel subscription', 'websocket,subscription,balance',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "channels": ["user.balance"],
        "messages": [
            {
                "type": "subscribe",
                "content": {
                    "id": 1,
                    "method": "subscribe",
                    "params": {
                        "channels": ["user.balance"]
                    }
                },
                "expectConfirmation": true
            },
            {
                "type": "expect",
                "timeout": 10000,
                "validate": {
                    "method": "${any}",
                    "params": {
                        "channel": "user.balance"
                    }
                }
            }
        ],
        "validation": {
            "messageOrder": false,
            "schemaValidation": true
        }
    }'
);

-- 4. Multiple Channel Subscription Test
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, true, 'false', 'websocket_market', 'multi_channel_subscription',
    'WS-004', 'Test multiple channel subscriptions', 'websocket,subscription,multi-channel',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "channels": ["user.order", "user.balance"],
        "messages": [
            {
                "type": "subscribe",
                "content": {
                    "id": 1,
                    "method": "subscribe",
                    "params": {
                        "channels": ["user.order", "user.balance"]
                    }
                },
                "expectConfirmation": true
            }
        ],
        "validation": {
            "messageOrder": false
        }
    }'
);

-- 5. Connection Limit Test (10 connections max)
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, false, 'false', 'websocket_market', 'connection_limit',
    'WS-005', 'Test WebSocket connection limit (max 10)', 'websocket,connection-limit,stress',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "connectionTest": {
            "connectionCount": 10,
            "maxConnections": 10,
            "testLimit": true
        },
        "messages": [],
        "connection": {
            "timeout": 5000
        }
    }'
);

-- 6. Performance Test - High Frequency Messages
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, false, 'false', 'websocket_market', 'performance_test',
    'WS-006', 'Test WebSocket performance with high frequency messages', 'websocket,performance',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "messages": [
            {
                "type": "subscribe",
                "content": {
                    "id": 1,
                    "method": "subscribe",
                    "params": {
                        "channels": ["user.order"]
                    }
                }
            }
        ],
        "metrics": {
            "maxLatency": 1000,
            "minThroughput": 100
        },
        "validation": {
            "messageOrder": false
        }
    }'
);

-- 7. Reconnection Test
INSERT INTO auto_case (
    enable, is_e2e, is_sanity, component, scenario,
    issue_key, description, label, path, method,
    profile_name, protocol, parameter
) VALUES (
    true, false, 'false', 'websocket_market', 'reconnection_test',
    'WS-007', 'Test WebSocket auto-reconnection', 'websocket,reconnection,resilience',
    '/exchange/v1/market', 'WEBSOCKET', 'uat', 'websocket',
    '{
        "wsUrl": "wss://uat-stream.3ona.co/exchange/v1/market",
        "connection": {
            "autoReconnect": true,
            "maxReconnectAttempts": 3,
            "timeout": 5000
        },
        "messages": [
            {
                "type": "send",
                "content": {
                    "id": 1,
                    "method": "public/heartbeat",
                    "code": 0
                }
            }
        ]
    }'
);

-- Create trigger to update timestamp
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_websocket_config_modtime
    BEFORE UPDATE ON auto_websocket_config
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();