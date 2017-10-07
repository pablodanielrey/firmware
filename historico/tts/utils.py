
import json
import datetime

class DateTimeEncoder(json.JSONEncoder):
    def default(self, obj):

        if isinstance(obj, datetime.datetime):
            return obj.isoformat()

        if isinstance(obj, datetime.date):
            return obj.isoformat()

        return json.JSONEncoder.default(self, obj)
