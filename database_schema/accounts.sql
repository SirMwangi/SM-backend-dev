CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          user_id UUID NOT NULL,

                          account_id TEXT NOT NULL,

                          account_name TEXT NOT NULL
                              CHECK (char_length(trim(account_name)) BETWEEN 1 AND 150),

                          institution TEXT NOT NULL
                              CHECK (char_length(trim(institution)) BETWEEN 1 AND 150),

                          account_type TEXT NOT NULL,

                          masked_identifier TEXT,

                          currency CHAR(3) NOT NULL,

                          ledger_balance NUMERIC(19,4) NOT NULL DEFAULT 0,

                          available_balance NUMERIC(19,4) NOT NULL DEFAULT 0,

                          credit_outstanding NUMERIC(19,4) NOT NULL DEFAULT 0,

                          credit_limit NUMERIC(19,4),

                          available_credit NUMERIC(19,4),

                          account_status TEXT NOT NULL DEFAULT 'active',

                          connection_status TEXT NOT NULL DEFAULT 'pending',

                          last_updated TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          data_source TEXT NOT NULL,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT accounts_user_fk
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id),

                          CONSTRAINT accounts_currency_check
                              CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT accounts_balance_check
        CHECK (ledger_balance >= 0),

    CONSTRAINT accounts_available_balance_check
        CHECK (available_balance >= 0),

    CONSTRAINT accounts_credit_outstanding_check
        CHECK (credit_outstanding >= 0),

    CONSTRAINT accounts_credit_limit_check
        CHECK (credit_limit IS NULL OR credit_limit >= 0),

    CONSTRAINT accounts_available_credit_check
        CHECK (available_credit IS NULL OR available_credit >= 0)
);